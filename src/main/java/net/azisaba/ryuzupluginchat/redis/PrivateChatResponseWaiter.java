package net.azisaba.ryuzupluginchat.redis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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

  private final Map<Long, ReachedPrivateChatData> alreadyReachedIdMap = new HashMap<>();

  public void register(long id, PrivateMessageData data, long timeout) {
    dataMap.put(id, data);
    timeouts.put(id, System.currentTimeMillis() + timeout);

    if (alreadyReachedIdMap.containsKey(id)) {
      ReachedPrivateChatData reachedData = alreadyReachedIdMap.remove(id);
      reached(
          reachedData.getId(),
          reachedData.getServer(),
          reachedData.getReceivedPlayerName(),
          reachedData.getReceivedPlayerDisplayName());
    }
  }

  public boolean isRegistered(long id) {
    return dataMap.containsKey(id);
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
                    Player p = Bukkit.getPlayer(data.getSentPlayerName());
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

  /**
   * @deprecated {@link #reached(long, String, String, String)}
   */
  @Deprecated
  protected void reached(long id, String server, String receivedPlayerName) {
    reached(id, server, receivedPlayerName, null);
  }

  protected void reached(long id, String server, String receivedPlayerName, String receivedPlayerDisplayName) {
    PrivateMessageData data = dataMap.getOrDefault(id, null);

    if (data != null && server.equals(plugin.getRpcConfig().getServerName())) {
      // It must be already processed so ignorable.
      dataMap.remove(id);
      timeouts.remove(id);
      return;
    }

    if (data == null) {
      ReachedPrivateChatData reachedData =
          new ReachedPrivateChatData(id, server, receivedPlayerName, receivedPlayerDisplayName);
      alreadyReachedIdMap.put(id, reachedData);
      return;
    }

    data.setReceiveServerName(server);
    data.setReceivedPlayerName(receivedPlayerName);
    data.setReceivedPlayerDisplayName(receivedPlayerDisplayName);

    plugin.getMessageProcessor().processPrivateMessage(data);

    dataMap.remove(id);
    timeouts.remove(id);
  }
}
