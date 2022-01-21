package ryuzupluginchat.ryuzupluginchat.discord;

import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.discordjson.json.MessageData;
import discord4j.discordjson.json.MessageEditRequest;
import discord4j.rest.entity.RestChannel;
import discord4j.rest.util.MultipartRequest;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import ryuzupluginchat.ryuzupluginchat.RyuZUPluginChat;
import ryuzupluginchat.ryuzupluginchat.message.data.ChannelChatMessageData;

@RequiredArgsConstructor
public class DiscordHandler {

  private final RyuZUPluginChat plugin;
  private final String token;

  private DiscordClient client;
  private GatewayDiscordClient gateway;

  public boolean init() {
    client = DiscordClient.create(token);
    gateway = client.login().block();

    if (gateway == null) {
      return false;
    }

    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> gateway.onDisconnect().block());
    return true;
  }

  public void connectLunaChatAndDiscordChannel(String lunaChatChannelName,
      Snowflake discordChannelId) {
    gateway.on(MessageCreateEvent.class).subscribe(event -> {
      try {
        Message message = event.getMessage();
        User user = message.getAuthor().orElse(null);
        if (user == null || user.isBot()) {
          return;
        }
        if (!message.getChannelId().equals(discordChannelId)) {
          return;
        }

        String content = message.getContent();
        if (content.length() <= 0) {
          return;
        }

        Member messageAuthor = message.getAuthorAsMember().block();
        if (messageAuthor == null) {
          return;
        }

        String senderName = messageAuthor.getNickname().orElse(messageAuthor.getUsername());

        ChannelChatMessageData data = plugin.getMessageDataFactory()
            .createChannelChatMessageDataFromDiscord(senderName,
                plugin.getRpcConfig().getDiscordLunaChatChannelName(), content);

        plugin.getPublisher().publishChannelChatMessage(data);
      } catch (Exception e) {
        e.printStackTrace();
      }
    });

    RestChannel discordMessageChannel = client.getChannelById(discordChannelId);

    plugin.getSubscriber().registerChannelChatConsumer((data) -> {
      if (data.isFromDiscord() || !data.getLunaChatChannelName().equals(lunaChatChannelName)) {
        return;
      }

      RyuZUPluginChat.newChain()
          .async(() -> {
            String message = data.getMessage();
            message = message.replace('@', '＠');
            MessageData jsonMessageData = discordMessageChannel.createMessage(message).block();

            if (jsonMessageData == null) {
              return;
            }

            long channelId = jsonMessageData.channelId().asLong();
            long messageId = jsonMessageData.id().asLong();

            String editedMessageContent = data.getPlayerName() + ": " + data.getMessage();

            MultipartRequest<MessageEditRequest> req = MultipartRequest.ofRequest(
                MessageEditRequest.builder().contentOrNull(editedMessageContent).build());

            client.getChannelService().editMessage(channelId, messageId, req).block();
          }).execute();
    });
  }

  public void disconnect() {
    gateway.logout().block();
  }
}
