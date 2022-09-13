package net.azisaba.ryuzupluginchat.listener;

import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import net.azisaba.ryuzupluginchat.RyuZUPluginChat;
import net.azisaba.ryuzupluginchat.event.AsyncPrivateMessageEvent;
import net.azisaba.ryuzupluginchat.event.AsyncPrivateMessageNotifyEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

@RequiredArgsConstructor
public class PrivateChatDebugListener implements Listener {

  private final RyuZUPluginChat plugin;

  @EventHandler
  public void onPrivateMessage(AsyncPrivateMessageEvent e) {
    plugin
        .getLogger()
        .info(
            "[Private-Chat(Debug)] Sending '"
                + e.getMessage().getSentPlayerName()
                + " to "
                + e.getMessage().getReceivedPlayerName()
                + "' private chat to: "
                + e.getRecipients().stream().map(Player::getName).collect(Collectors.joining(", ")));
  }

  @EventHandler
  public void onAsyncPrivateMessageNotify(AsyncPrivateMessageNotifyEvent e) {
    plugin
        .getLogger()
        .info(
            "[Private-Chat(Debug)] Sending '"
                + e.getMessage().getSentPlayerName()
                + " to "
                + e.getMessage().getReceivedPlayerName()
                + "' private chat notify to: "
                + e.getRecipients().stream()
                    .map(Player::getName)
                    .collect(Collectors.joining(", ")));
  }
}
