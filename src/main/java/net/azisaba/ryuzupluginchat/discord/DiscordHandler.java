package net.azisaba.ryuzupluginchat.discord;

import com.github.ucchyocean.lc3.LunaChat;
import com.github.ucchyocean.lc3.LunaChatAPI;
import com.github.ucchyocean.lc3.channel.Channel;
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
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import net.azisaba.ryuzupluginchat.RyuZUPluginChat;
import net.azisaba.ryuzupluginchat.discord.data.ChannelChatSyncData;
import net.azisaba.ryuzupluginchat.discord.data.GlobalChatSyncData;
import net.azisaba.ryuzupluginchat.discord.data.PrivateChatSyncData;
import net.azisaba.ryuzupluginchat.message.data.ChannelChatMessageData;
import net.azisaba.ryuzupluginchat.message.data.GlobalMessageData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

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

  public void connectUsing(DiscordMessageConnection connectionData) {
    if (connectionData.getGlobalChatSyncData().isEnabled()) {
      GlobalChatSyncData data = connectionData.getGlobalChatSyncData();
      if (data.isDiscordInputEnabled()) {
        registerDiscordToGlobal(connectionData.getDiscordChannelId());
      }

      registerGlobalToDiscord(connectionData.getDiscordChannelId(), data.isVoiceChatMode());
    }

    if (connectionData.getChannelChatSyncData().isEnabled()) {
      ChannelChatSyncData data = connectionData.getChannelChatSyncData();

      if (data.isDiscordInputEnabled()) {
        registerDiscordToChannels(connectionData.getDiscordChannelId(), data);
      }

      registerLunaChatChannelToDiscord(
          data, connectionData.getDiscordChannelId(), data.isVoiceChatMode());
    }

    if (connectionData.getPrivateChatSyncData().isEnabled()) {
      PrivateChatSyncData data = connectionData.getPrivateChatSyncData();
      registerPrivateToDiscord(connectionData.getDiscordChannelId(), data.isVoiceChatMode());
    }
  }

  private void registerDiscordToGlobal(Snowflake discordChannelId) {
    gateway
        .on(MessageCreateEvent.class)
        .subscribe(
            event -> {
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
                content = removeUrl(content);

                Member messageAuthor = message.getAuthorAsMember().block();
                if (messageAuthor == null) {
                  return;
                }

                String senderName = messageAuthor.getNickname().orElse(messageAuthor.getUsername());

                GlobalMessageData data =
                    plugin
                        .getMessageDataFactory()
                        .createGlobalMessageDataFromDiscord(senderName, content);

                plugin.getPublisher().publishGlobalMessage(data);
              } catch (Exception e) {
                e.printStackTrace();
              }
            });
  }

  private void registerDiscordToChannels(
      Snowflake discordChannelId, ChannelChatSyncData channelChatSyncData) {
    gateway
        .on(MessageCreateEvent.class)
        .subscribe(
            event -> {
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
                final String urlDeletedContent = removeUrl(content);

                Member messageAuthor = message.getAuthorAsMember().block();
                if (messageAuthor == null) {
                  return;
                }

                String senderName = messageAuthor.getNickname().orElse(messageAuthor.getUsername());

                RyuZUPluginChat.newChain()
                    .asyncFirst(
                        () -> {
                          LunaChatAPI api = LunaChat.getAPI();
                          List<Channel> channelList = new ArrayList<>();
                          for (Channel channel : api.getChannels()) {
                            if (channelChatSyncData.isMatch(channel.getName())) {
                              channelList.add(channel);
                            }
                          }
                          return channelList;
                        })
                    .asyncLast(
                        (list) -> {
                          for (Channel ch : list) {
                            ChannelChatMessageData data =
                                plugin
                                    .getMessageDataFactory()
                                    .createChannelChatMessageDataFromDiscord(
                                        senderName, ch.getName(), urlDeletedContent);
                            plugin.getPublisher().publishChannelChatMessage(data);
                          }
                        })
                    .execute();
              } catch (Exception e) {
                e.printStackTrace();
              }
            });
  }

  private void registerGlobalToDiscord(Snowflake discordChannelId, boolean vcMode) {
    RestChannel targetChannel = client.getChannelById(discordChannelId);
    plugin
        .getSubscriber()
        .registerPublicConsumer(
            (data) -> {
              if (data.isFromDiscord()) {
                return;
              }

              RyuZUPluginChat.newChain()
                  .async(
                      () -> {
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
                                  MessageEditRequest.builder()
                                      .contentOrNull(editedMessageContent)
                                      .build());

                          client.getChannelService().editMessage(channelId, messageId, req).block();
                        }
                      })
                  .execute();
            });
  }

  private void registerLunaChatChannelToDiscord(
      ChannelChatSyncData channelChatSyncData, Snowflake discordChannelId, boolean vcMode) {
    RestChannel targetChannel = client.getChannelById(discordChannelId);
    plugin
        .getSubscriber()
        .registerChannelChatConsumer(
            (data) -> {
              if (data.isFromDiscord()) {
                return;
              }
              if (!channelChatSyncData.isMatch(data.getLunaChatChannelName())) {
                return;
              }

              RyuZUPluginChat.newChain()
                  .async(
                      () -> {
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
                                  MessageEditRequest.builder()
                                      .contentOrNull(editedMessageContent)
                                      .build());

                          client.getChannelService().editMessage(channelId, messageId, req).block();
                        }
                      })
                  .execute();
            });
  }

  private void registerPrivateToDiscord(Snowflake discordChannelId, boolean vcMode) {
    RestChannel targetChannel = client.getChannelById(discordChannelId);
    plugin
        .getSubscriber()
        .registerTellConsumer(
            (data) -> {
              RyuZUPluginChat.newChain()
                  .async(
                      () -> {
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
                              plugin
                                  .getPlayerUUIDMapContainer()
                                  .getNameFromUUID(data.getReceivedPlayerUUID());
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
                                  MessageEditRequest.builder()
                                      .contentOrNull(editedMessageContent)
                                      .build());

                          client.getChannelService().editMessage(channelId, messageId, req).block();
                        }
                      })
                  .execute();
            });
  }

  private String sanitize(String message) {
    message = message.replace("@", "\\@");
    message = ChatColor.translateAlternateColorCodes('&', message);
    message = ChatColor.stripColor(message);
    return message;
  }

  private String removeUrl(String msg) {
    String urlPattern =
        "((https?|ftp|gopher|telnet|file|Unsure|http):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)";
    Pattern p = Pattern.compile(urlPattern, Pattern.CASE_INSENSITIVE);
    Matcher m = p.matcher(msg);
    int i = 0;
    while (m.find()) {
      msg = msg.replaceAll(m.group(i), "<URL>").trim();
      i++;
    }
    return msg;
  }

  public void disconnect() {
    if (gateway != null) {
      gateway.logout().block();
    }
  }
}
