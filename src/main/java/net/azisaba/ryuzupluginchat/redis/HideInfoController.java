package net.azisaba.ryuzupluginchat.redis;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import net.azisaba.ryuzupluginchat.util.JedisUtils;
import redis.clients.jedis.JedisPool;

@RequiredArgsConstructor
public class HideInfoController {

  private final JedisPool jedisPool;

  private final String groupName;

  private final ReentrantLock lock = new ReentrantLock(true);

  private final HashMap<UUID, Set<UUID>> hideMap = new HashMap<>();
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

  public Set<UUID> getPlayersWhoHide(UUID uuid) {
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

  public boolean isHidingPlayer(UUID actor, UUID target) {
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
      Map<String, String> rawData = JedisUtils.executeUsingJedisPoolWithReturn(jedisPool,
          (jedis) -> jedis.hgetAll("rpc:" + groupName + ":hide-map"));
      for (String key : rawData.keySet()) {
        UUID keyUUID = UUID.fromString(key);
        Set<UUID> uuidList = Arrays.stream(rawData.get(key).split(","))
            .map(UUID::fromString)
            .collect(Collectors.toSet());
        hideMap.put(keyUUID, uuidList);
        keyLastUpdatedMilliSeconds = System.currentTimeMillis();
      }
    } finally {
      lock.unlock();
    }
  }

  private void updateRedisInfo(UUID uuid) {
    Set<String> uuidSetStr = hideMap.get(uuid).stream()
        .map(UUID::toString)
        .collect(Collectors.toSet());

    String oneLine = String.join(",", uuidSetStr);

    JedisUtils.executeUsingJedisPool(jedisPool,
        (jedis) -> jedis.hset("rpc:" + groupName + ":hide-map", uuid.toString(), oneLine));
  }
}
