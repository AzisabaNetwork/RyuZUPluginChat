package net.azisaba.ryuzupluginchat.redis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.locks.ReentrantLock;
import lombok.RequiredArgsConstructor;
import net.azisaba.ryuzupluginchat.RyuZUPluginChat;
import net.azisaba.ryuzupluginchat.message.data.PrivateMessageData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

@RequiredArgsConstructor
public class PrivateChatResponseWaiter {

  private final RyuZUPluginChat plugin;

  private final HashMap<Long, PrivateMessageData> dataMap = new HashMap<>();
  private final HashMap<Long, Long> timeouts = new HashMap<>();

  private final ReentrantLock lock = new ReentrantLock();

  public void register(long id, PrivateMessageData data, long timeout) {
    lock.lock();
    try {
      dataMap.put(id, data);
      timeouts.put(id, System.currentTimeMillis() + timeout);
    } finally {
      lock.unlock();
    }
  }

  public void runTimeoutDetectTask(JavaPlugin plugin) {
    Bukkit.getScheduler()
        .runTaskTimer(
            plugin,
            () -> {
              for (long id : new HashSet<>(timeouts.keySet())) {
                if (timeouts.get(id) < System.currentTimeMillis()) {
                  PrivateMessageData data = dataMap.remove(id);
                  timeouts.remove(id);

                  if (data != null) {
                    Player p = Bukkit.getPlayer(data.getSentPlayerUuid());
                    if (p != null) {
                      p.sendMessage(
                          ChatColor.YELLOW + "[Error] " + ChatColor.RED + "個人チャットの送信に失敗しました");
                    }
                  }
                }
              }
            },
            10L,
            10L);
  }

  protected void reached(PrivateMessageData data) {
    boolean deleted;

    lock.lock();
    try {
      deleted = dataMap.remove(data.getId()) != null;
      timeouts.remove(data.getId());
    } finally {
      lock.unlock();
    }
    
    if (deleted) {
      plugin.getMessageProcessor().notifyDeliveredPrivateMessage(data);
    }
  }
}
