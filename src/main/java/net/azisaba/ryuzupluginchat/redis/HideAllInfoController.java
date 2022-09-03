package net.azisaba.ryuzupluginchat.redis;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;
import lombok.RequiredArgsConstructor;
import net.azisaba.ryuzupluginchat.RyuZUPluginChat;
import org.bukkit.Bukkit;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@RequiredArgsConstructor
public class HideAllInfoController {

  private final HashMap<UUID, Long> hideAllMap = new HashMap<>();
  private final ReentrantLock lock = new ReentrantLock();

  private final RyuZUPluginChat plugin;
  private final JedisPool jedisPool;

  public void refreshAllAsync() {
    Bukkit.getOnlinePlayers().forEach(p -> refreshHideAllInfoAsync(p.getUniqueId()));
  }

  public boolean isHideAllPlayer(UUID uuid) {
    lock.lock();
    try {
      if (!hideAllMap.containsKey(uuid)) {
        return false;
      }
      return hideAllMap.get(uuid) > System.currentTimeMillis();
    } finally {
      lock.unlock();
    }
  }

  public void refreshHideAllInfoAsync(UUID uuid) {
    RyuZUPluginChat.newChain()
        .async(
            () -> {
              try (Jedis jedis = jedisPool.getResource()) {
                String key =
                    "rpc:" + plugin.getRpcConfig().getGroupName() + ":hideall:" + uuid.toString();
                String value = jedis.get(key);

                if (value == null) {
                  updateMilliseconds(uuid, 0);
                  return;
                }

                long milliSecond;
                try {
                  milliSecond = Long.parseLong(value);
                } catch (NumberFormatException ex) {
                  plugin
                      .getLogger()
                      .warning(
                          "Invalid hideall milliseconds has been received by redis server. ( "
                              + value
                              + " )");
                  return;
                }

                updateMilliseconds(uuid, milliSecond);
              }
            })
        .execute();
  }

  public void discardHideAllInfo(UUID uuid) {
    updateMilliseconds(uuid, 0);
  }

  private void updateMilliseconds(UUID uuid, long value) {
    lock.lock();
    try {
      if (value <= System.currentTimeMillis()) {
        hideAllMap.remove(uuid);
      } else {
        hideAllMap.put(uuid, value);
      }
    } finally {
      lock.unlock();
    }
  }
}
