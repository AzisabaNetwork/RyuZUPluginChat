package net.azisaba.ryuzupluginchat.redis;

import java.util.HashMap;
import java.util.HashSet;
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

  public void register(long id, PrivateMessageData data, long timeout) {
    dataMap.put(id, data);
    timeouts.put(id, System.currentTimeMillis() + timeout);
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
    plugin.getMessageProcessor().notifyDeliveredPrivateMessage(data);

    dataMap.remove(data.getId());
    timeouts.remove(data.getId());
  }
}
