package net.azisaba.ryuzupluginchat.redis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;
import lombok.RequiredArgsConstructor;
import net.azisaba.ryuzupluginchat.RyuZUPluginChat;
import net.azisaba.ryuzupluginchat.util.JedisUtils;
import org.bukkit.entity.Player;
import redis.clients.jedis.JedisPool;

@RequiredArgsConstructor
public class PlayerUUIDMapContainer {

  private final RyuZUPluginChat plugin;

  private final JedisPool jedisPool;

  private final String groupName;

  private final Map<String, String> playerCache = new HashMap<>();
  private Map<String, String> playerCacheCaseSensitive = new HashMap<>();
  private long lastCacheUpdated;

  private final ReentrantLock lock = new ReentrantLock(true);

  public void register(String name, UUID uuid) {
    JedisUtils.executeUsingJedisPool(
        jedisPool, (jedis) -> jedis.hset("rpc:" + groupName + ":uuid-map", name, uuid.toString()));

    lock.lock();
    try {
      playerCache.put(name.toLowerCase(), uuid.toString());
      playerCacheCaseSensitive.put(name, uuid.toString());
    } finally {
      lock.unlock();
    }
  }

  public void register(Player p) {
    register(p.getName(), p.getUniqueId());
  }

  public void unregister(String name) {
    JedisUtils.executeUsingJedisPool(
        jedisPool, (jedis) -> jedis.hdel("rpc:" + groupName + ":uuid-map", name));

    lock.lock();
    try {
      playerCache.remove(name.toLowerCase(Locale.ROOT));
      playerCacheCaseSensitive.remove(name);
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
      String uuidStr = playerCache.getOrDefault(name.toLowerCase(), null);

      if (uuidStr == null) {
        return null;
      }
      try {
        return UUID.fromString(uuidStr);
      } catch (IllegalArgumentException e) {
        plugin.getLogger().warning("Received invalid UUID from Redis server ( " + uuidStr + " )");
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
      return new HashSet<>(playerCacheCaseSensitive.keySet());
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
      for (String name : playerCacheCaseSensitive.keySet()) {
        String uuidStr = playerCacheCaseSensitive.get(name);
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
        playerCacheCaseSensitive =
            JedisUtils.executeUsingJedisPoolWithReturn(
                jedisPool, (jedis) -> jedis.hgetAll("rpc:" + groupName + ":uuid-map"));
        playerCache.clear();
        for (String mcid : playerCacheCaseSensitive.keySet()) {
          playerCache.put(mcid.toLowerCase(), playerCacheCaseSensitive.get(mcid));
        }
        lastCacheUpdated = System.currentTimeMillis();
      } finally {
        lock.unlock();
      }
    }
  }
}
