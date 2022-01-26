package ryuzupluginchat.ryuzupluginchat.redis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import redis.clients.jedis.Jedis;
import ryuzupluginchat.ryuzupluginchat.RyuZUPluginChat;

@RequiredArgsConstructor
public class PlayerUUIDMapContainer {

  private final RyuZUPluginChat plugin;

  private final Jedis jedis;

  private final String groupName;

  private Map<String, String> playerCache = new HashMap<>();
  private long lastCacheUpdated;

  private final ReentrantLock lock = new ReentrantLock(true);

  public void register(String name, UUID uuid) {
    jedis.hset("rpc:" + groupName + ":uuid-map", name.toLowerCase(Locale.ROOT), uuid.toString());

    lock.lock();
    try {
      playerCache.put(name.toLowerCase(Locale.ROOT), uuid.toString());
    } finally {
      lock.unlock();
    }
  }

  public void register(Player p) {
    register(p.getName(), p.getUniqueId());
  }

  public void unregister(String name) {
    jedis.hdel("rpc:" + groupName + ":uuid-map", name.toLowerCase());

    lock.lock();
    try {
      playerCache.remove(name.toLowerCase(Locale.ROOT));
    } finally {
      lock.unlock();
    }
  }

  public void unregister(Player p) {
    unregister(p.getName());
  }

  public UUID getUUID(String name) {
    updateCacheIfOutdated();
    lock.lock();
    try {
      String uuidStr = playerCache.getOrDefault(name, null);

      if (uuidStr == null) {
        return null;
      }
      try {
        return UUID.fromString(uuidStr);
      } catch (IllegalArgumentException e) {
        plugin.getLogger()
            .warning(
                "Received invalid UUID from Redis server ( " + uuidStr + " )");
        return null;
      }
    } finally {
      lock.unlock();
    }
  }

  public Set<String> getAllNames() {
    updateCacheIfOutdated();
    lock.lock();
    try {
      return new HashSet<>(playerCache.keySet());
    } finally {
      lock.unlock();
    }
  }

  public boolean isOnline(UUID uuid) {
    updateCacheIfOutdated();
    lock.lock();
    try {
      return playerCache.containsValue(uuid.toString());
    } finally {
      lock.unlock();
    }
  }

  public String getNameFromUUID(UUID uuid) {
    updateCacheIfOutdated();

    lock.lock();
    try {
      for (String name : playerCache.keySet()) {
        String uuidStr = playerCache.get(name);
        if (uuidStr.equals(uuid.toString())) {
          return name;
        }
      }
    } finally {
      lock.unlock();
    }

    return null;
  }

  private void updateCacheIfOutdated() {
    if (lastCacheUpdated + 5000L < System.currentTimeMillis()) {
      lock.lock();
      try {
        playerCache = jedis.hgetAll("rpc:" + groupName + ":uuid-map");
        lastCacheUpdated = System.currentTimeMillis();
      } finally {
        lock.unlock();
      }
    }
  }
}
