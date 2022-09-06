package net.azisaba.ryuzupluginchat.listener;

import lombok.RequiredArgsConstructor;
import net.azisaba.ryuzupluginchat.RyuZUPluginChat;
import net.azisaba.ryuzupluginchat.event.AsyncPrivateMessageEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.stream.Collectors;

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
}
