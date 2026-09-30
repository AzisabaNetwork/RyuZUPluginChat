package net.azisaba.ryuzupluginchat.discord.deliverer;

import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import net.azisaba.ryuzupluginchat.RyuZUPluginChat;
import net.azisaba.ryuzupluginchat.message.data.ChannelChatMessageData;
import net.azisaba.ryuzupluginchat.message.data.GlobalMessageData;
import net.azisaba.ryuzupluginchat.message.data.PrivateMessageData;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import org.bukkit.ChatColor;

@RequiredArgsConstructor
public class ServerChatMessageDeliverer {

  private final RyuZUPluginChat plugin;
  private final JDA client;

  public void sendToDiscord(GlobalMessageData data, MessageChannel targetChannel, boolean vcMode) {
    String message = sanitize(vcMode ? data.getMessage() : data.format());
    String editedMessage = vcMode ? sanitize(data.format()) : null;
    queueMessage(message, editedMessage, targetChannel);
  }

  public void sendToDiscord(ChannelChatMessageData data, MessageChannel targetChannel, boolean vcMode) {
    String message = sanitize(vcMode ? data.getMessage() : data.format());
    String editedMessage = vcMode ? sanitize(data.format()) : null;
    queueMessage(message, editedMessage, targetChannel);
  }

  public void sendToDiscord(PrivateMessageData data, MessageChannel targetChannel, boolean vcMode) {
    String message = sanitize(vcMode ? data.getMessage() : data.format());

    // 名前がnullだとUUIDが表示されてしまうのでmcidに変更する
    if (data.getReceivedPlayerName() == null) {
      String receivePlayerName =
          plugin.getPlayerUUIDMapContainer().getNameFromUUID(data.getReceivedPlayerUUID());
      if (receivePlayerName != null) {
        message = message.replace(data.getReceivedPlayerUUID().toString(), receivePlayerName);
      }
    }

    String editedMessage = vcMode ? sanitize(data.format()) : null;
    queueMessage(message, editedMessage, targetChannel);
  }

  // Sends the message, then rewrites it with editedMessage when vcMode is on.
  private void queueMessage(String message, String editedMessage, MessageChannel targetChannel) {
    targetChannel
        .sendMessage(message)
        .queue(
            (sentMessage) -> {
              if (editedMessage != null) {
                sentMessage.editMessage(editedMessage).queue(null, handleFailure());
              }
            },
            handleFailure());
  }

  private Consumer<Throwable> handleFailure() {
    return (throwable) ->
        plugin.getSLF4JLogger().error("Failed to send a message to Discord", throwable);
  }

  private static String sanitize(String message) {
    message = message.replace("@", "\\@");
    message = ChatColor.translateAlternateColorCodes('&', message);
    message = ChatColor.stripColor(message);
    return message;
  }
}
