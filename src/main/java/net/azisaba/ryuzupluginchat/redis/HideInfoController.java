package net.azisaba.ryuzupluginchat.redis;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import net.azisaba.ryuzupluginchat.RyuZUPluginChat;
import net.azisaba.ryuzupluginchat.util.JedisUtils;
import redis.clients.jedis.JedisPool;

@RequiredArgsConstructor
public class HideInfoController {

  private final JedisPool jedisPool;
  private final String groupName;
  private final ReentrantLock lock = new ReentrantLock(true);

  // Player : Set of players who hide the <Player>
  private final HashMap<UUID, Set<UUID>> hideMap = new HashMap<>();

  // Player : Set of players who are hidden by <Player>
  private final Map<UUID, Set<UUID>> reverseHideMap = new HashMap<>();
  private long keyLastUpdatedMilliSeconds = -1L;

  public void setHide(UUID actor, UUID target) {
    lock.lock();
    try {
      Set<UUID> hideList = hideMap.getOrDefault(target, null);
      if (hideList == null) {
        hideList = new HashSet<>();
      }

      if (hideList.contains(actor)) {
        return;
      }

      hideList.add(actor);
      hideMap.put(target, hideList);
    } finally {
      lock.unlock();
    }

    updateRedisInfo(target);
  }

  public void removeHide(UUID actor, UUID target) {
    lock.lock();
    try {
      Set<UUID> hideList = hideMap.getOrDefault(target, Collections.emptySet());

      if (!hideList.contains(actor)) {
        return;
      }

      hideList.remove(actor);
      hideMap.put(target, hideList);
    } finally {
      lock.unlock();
    }

    updateRedisInfo(target);
  }

  /**
   * Returns the set of players who hides the target player.
   * @param uuid The target player
   * @return set of UUIDs
   */
  public Set<UUID> getPlayersWhoHide(UUID uuid) {
    if (keyLastUpdatedMilliSeconds + (1000L * 10) < System.currentTimeMillis()) {
      RyuZUPluginChat.newChain().delay(1, TimeUnit.MILLISECONDS).async(this::updateCache).execute();
    }

    lock.lock();
    try {
      Set<UUID> uuidSet = hideMap.getOrDefault(uuid, null);
      if (uuidSet == null) {
        uuidSet = new HashSet<>();
      }
      return uuidSet;
    } finally {
      lock.unlock();
    }
  }

  /**
   * Returns the set of players who are hidden by the target player.
   * @param uuid the target player
   * @return set of UUIDs
   */
  public Set<UUID> getHiddenPlayersBy(UUID uuid) {
    if (keyLastUpdatedMilliSeconds + (1000L * 10) < System.currentTimeMillis()) {
      RyuZUPluginChat.newChain().delay(1, TimeUnit.MILLISECONDS).async(this::updateCache).execute();
    }

    lock.lock();
    try {
      return reverseHideMap.getOrDefault(uuid, Collections.emptySet());
    } finally {
      lock.unlock();
    }
  }

  public boolean isHidingPlayer(UUID actor, UUID target) {
    if (keyLastUpdatedMilliSeconds + (1000L * 10) < System.currentTimeMillis()) {
      RyuZUPluginChat.newChain().delay(1, TimeUnit.MILLISECONDS).async(this::updateCache).execute();
    }

    lock.lock();
    try {
      return hideMap.getOrDefault(target, Collections.emptySet()).contains(actor);
    } finally {
      lock.unlock();
    }
  }

  public void updateCache() {
    lock.lock();
    try {
      hideMap.clear();
      reverseHideMap.clear();
      // Player : List of players who hide the <Player>
      Map<String, String> rawData =
          JedisUtils.executeUsingJedisPoolWithReturn(
              jedisPool, (jedis) -> jedis.hgetAll("rpc:" + groupName + ":hide-map"));
      for (String key : rawData.keySet()) {
        UUID keyUUID = UUID.fromString(key);
        if (!rawData.get(key).isEmpty()) {
          Set<UUID> uuidList =
              Arrays.stream(rawData.get(key).split(","))
                  .map(UUID::fromString)
                  .collect(Collectors.toSet());
          hideMap.put(keyUUID, uuidList);
          for (UUID uuid : uuidList) {
            reverseHideMap.computeIfAbsent(uuid, k -> new HashSet<>()).add(keyUUID);
          }
        }
      }
      keyLastUpdatedMilliSeconds = System.currentTimeMillis();
    } finally {
      lock.unlock();
    }
  }

  private void updateRedisInfo(UUID uuid) {
    Set<String> uuidSetStr =
        hideMap.get(uuid).stream().map(UUID::toString).collect(Collectors.toSet());

    String oneLine = String.join(",", uuidSetStr);

    if (oneLine.length() == 0) {
      JedisUtils.executeUsingJedisPool(
          jedisPool, (jedis) -> jedis.hdel("rpc:" + groupName + ":hide-map", uuid.toString()));
    } else {
      JedisUtils.executeUsingJedisPool(
          jedisPool,
          (jedis) -> jedis.hset("rpc:" + groupName + ":hide-map", uuid.toString(), oneLine));
    }
  }
}
