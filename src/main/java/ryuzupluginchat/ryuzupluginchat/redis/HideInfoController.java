package ryuzupluginchat.ryuzupluginchat.redis;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import redis.clients.jedis.Jedis;

@RequiredArgsConstructor
public class HideInfoController {

  private final Jedis jedis;

  private final String groupName;

  private final ReentrantLock lock = new ReentrantLock(true);

  private final HashMap<UUID, Set<UUID>> hideMap = new HashMap<>();
  private long keyLastUpdatedMilliSeconds = -1L;

  public void setHide(UUID actor, UUID target) {
    lock.lock();
    try {
      Set<UUID> hideList = hideMap.getOrDefault(target, Collections.emptySet());

      if (hideList.contains(actor)) {
        return;
      }

      hideList.add(actor);
      hideMap.put(target, hideList);
    } finally {
      lock.unlock();
    }

    Set<String> uuidSetStr = hideMap.get(target).stream()
        .map(UUID::toString)
        .collect(Collectors.toSet());

    String oneLine = String.join(",", uuidSetStr);

    jedis.hset("rpc:" + groupName + ":hide-map", target.toString(), oneLine);
  }

  public Set<UUID> getPlayersWhoHide(UUID uuid) {
    lock.lock();
    try {
      return hideMap.getOrDefault(uuid, Collections.emptySet());
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
      Map<String, String> rawData = jedis.hgetAll("rpc:" + groupName + ":hide-map");
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
}
