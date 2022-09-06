package net.azisaba.ryuzupluginchat.redis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import net.azisaba.ryuzupluginchat.RyuZUPluginChat;
import net.azisaba.ryuzupluginchat.event.AsyncPrivateMessageEvent;
import net.azisaba.ryuzupluginchat.message.data.PrivateMessageData;
import net.azisaba.ryuzupluginchat.util.TaskSchedulingUtils;
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
    if (data == null) {
      return;
    }
    data.setReceiveServerName(server);
    data.setReceivedPlayerName(receivedPlayerName);
    data.setReceivedPlayerDisplayName(receivedPlayerDisplayName);

    String receiverName =
        plugin.getPlayerUUIDMapContainer().getNameFromUUID(data.getReceivedPlayerUUID());

    Player sentPlayer = Bukkit.getPlayer(data.getSentPlayerName());
    if (sentPlayer == null) {
      return;
    }

    String message = data.format();

    Set<Player> recipients;
    if (receiverName != null) {
      recipients = TaskSchedulingUtils.getSynchronously(
          () ->
              Bukkit.getOnlinePlayers().stream()
                  .filter(p -> p.hasPermission("rpc.op"))
                  .filter(
                      p ->
                          !p.getUniqueId().equals(data.getReceivedPlayerUUID())
                              && !p.getUniqueId().equals(sentPlayer.getUniqueId()))
                  .filter(p -> !plugin.getPrivateChatInspectHandler().isDisabled(p.getUniqueId()))
                  .collect(Collectors.toCollection(HashSet<Player>::new))
      ).join();
    } else {
      recipients = new HashSet<>();
    }

    recipients.add(sentPlayer);

    plugin.getLogger().info("[Private-Chat] " + ChatColor.stripColor(message));

    AsyncPrivateMessageEvent event = new AsyncPrivateMessageEvent(data, recipients);
    Bukkit.getPluginManager().callEvent(event);
    if (event.isCancelled()) {
      return;
    }

    for (Player player : event.getRecipients()) {
      player.sendMessage(message);
    }

    dataMap.remove(id);
    timeouts.remove(id);
  }
}
