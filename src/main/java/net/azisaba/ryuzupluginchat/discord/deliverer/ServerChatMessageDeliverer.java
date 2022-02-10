package net.azisaba.ryuzupluginchat.discord.deliverer;

import discord4j.core.DiscordClient;
import discord4j.discordjson.json.MessageData;
import discord4j.discordjson.json.MessageEditRequest;
import discord4j.rest.entity.RestChannel;
import discord4j.rest.util.MultipartRequest;
import lombok.RequiredArgsConstructor;
import net.azisaba.ryuzupluginchat.RyuZUPluginChat;
import net.azisaba.ryuzupluginchat.message.data.ChannelChatMessageData;
import net.azisaba.ryuzupluginchat.message.data.GlobalMessageData;
import net.azisaba.ryuzupluginchat.message.data.PrivateMessageData;
import org.bukkit.ChatColor;

@RequiredArgsConstructor
public class ServerChatMessageDeliverer {

  private final RyuZUPluginChat plugin;
  private final DiscordClient client;

  public void sendToDiscord(GlobalMessageData data, RestChannel targetChannel, boolean vcMode) {
    String message;
    if (vcMode) {
      message = data.getMessage();
    } else {
      message = data.format();
    }
    message = sanitize(message);
    MessageData jsonMessageData = targetChannel.createMessage(message).block();

    if (vcMode) {
      if (jsonMessageData == null) {
        return;
      }

      long channelId = jsonMessageData.channelId().asLong();
      long messageId = jsonMessageData.id().asLong();

      String editedMessageContent = sanitize(data.format());

      MultipartRequest<MessageEditRequest> req =
          MultipartRequest.ofRequest(
              MessageEditRequest.builder().contentOrNull(editedMessageContent).build());

      client.getChannelService().editMessage(channelId, messageId, req).block();
    }
  }

  public void sendToDiscord(
      ChannelChatMessageData data, RestChannel targetChannel, boolean vcMode) {
    String message;
    if (vcMode) {
      message = data.getMessage();
    } else {
      message = data.format();
    }
    message = sanitize(message);
    MessageData jsonMessageData = targetChannel.createMessage(message).block();

    if (vcMode) {
      if (jsonMessageData == null) {
        return;
      }

      long channelId = jsonMessageData.channelId().asLong();
      long messageId = jsonMessageData.id().asLong();

      String editedMessageContent = sanitize(data.format());

      MultipartRequest<MessageEditRequest> req =
          MultipartRequest.ofRequest(
              MessageEditRequest.builder().contentOrNull(editedMessageContent).build());

      client.getChannelService().editMessage(channelId, messageId, req).block();
    }
  }

  public void sendtoDiscord(PrivateMessageData data, RestChannel targetChannel, boolean vcMode) {
    String message;
    if (vcMode) {
      message = data.getMessage();
    } else {
      message = data.format();
    }
    message = sanitize(message);

    // 名前がnullだとUUIDが表示されてしまうのでmcidに変更する
    if (data.getReceivedPlayerName() == null) {
      String receivePlayerName =
          plugin.getPlayerUUIDMapContainer().getNameFromUUID(data.getReceivedPlayerUUID());
      if (receivePlayerName != null) {
        message =
            message.replace(
                data.getReceivedPlayerUUID().toString(),
                receivePlayerName); // TODO もうすこしきれいな処理にできるはず
      }
    }

    MessageData jsonMessageData = targetChannel.createMessage(message).block();

    if (vcMode) {
      if (jsonMessageData == null) {
        return;
      }

      long channelId = jsonMessageData.channelId().asLong();
      long messageId = jsonMessageData.id().asLong();

      String editedMessageContent = sanitize(data.format());

      MultipartRequest<MessageEditRequest> req =
          MultipartRequest.ofRequest(
              MessageEditRequest.builder().contentOrNull(editedMessageContent).build());

      client.getChannelService().editMessage(channelId, messageId, req).block();
    }
  }

  private String sanitize(String message) {
    message = message.replace("@", "\\@");
    message = ChatColor.translateAlternateColorCodes('&', message);
    message = ChatColor.stripColor(message);
    return message;
  }
}
