package net.azisaba.ryuzupluginchat.redis;

import lombok.RequiredArgsConstructor;
import net.azisaba.ryuzupluginchat.RyuZUPluginChat;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

@RequiredArgsConstructor
public class VanishController {

  private final Set<UUID> vanishedPlayers = new HashSet<>();
  private final ReentrantLock lock = new ReentrantLock();

  private final JedisPool jedisPool;
  private final String groupName;

  public boolean isVanished(UUID uuid) {
    lock.lock();
    try {
      return vanishedPlayers.contains(uuid);
    } finally {
      lock.unlock();
    }
  }

  public void refreshAllAsync() {
    RyuZUPluginChat.newChain()
        .async(
            () -> {
              try (Jedis jedis = jedisPool.getResource()) {
                String keyPrefix =
                    "rpc:" + groupName + ":vanish:";

                try {
                  lock.lock();
                  vanishedPlayers.clear();
                  for (String s : jedis.keys(keyPrefix + "*")) {
                    try {
                      UUID uuid = UUID.fromString(s.replace(keyPrefix, ""));
                      vanishedPlayers.add(uuid);
                    } catch (IllegalArgumentException ignored) {
                    }
                  }
                } finally {
                  lock.unlock();
                }
              }
            })
        .execute();
  }

  public void setVanished(UUID uuid, boolean vanished) {
    lock.lock();
    try {
      if (vanished) {
        vanishedPlayers.add(uuid);
      } else {
        vanishedPlayers.remove(uuid);
      }
      RyuZUPluginChat.newChain()
          .async(
              () -> {
                try (Jedis jedis = jedisPool.getResource()) {
                  String key =
                      "rpc:" + groupName + ":vanish:" + uuid.toString();
                  if (vanished) {
                    jedis.set(key, "true");
                  } else {
                    jedis.del(key);
                  }
                }
              })
          .execute();
    } finally {
      lock.unlock();
    }
  }
}
