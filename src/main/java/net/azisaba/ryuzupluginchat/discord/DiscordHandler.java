package net.azisaba.ryuzupluginchat.discord;

import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.rest.entity.RestChannel;
import lombok.RequiredArgsConstructor;
import net.azisaba.ryuzupluginchat.RyuZUPluginChat;
import net.azisaba.ryuzupluginchat.discord.data.ChannelChatSyncData;
import net.azisaba.ryuzupluginchat.discord.data.GlobalChatSyncData;
import net.azisaba.ryuzupluginchat.discord.data.PrivateChatSyncData;
import net.azisaba.ryuzupluginchat.discord.deliverer.DiscordMessageDeliverer;
import net.azisaba.ryuzupluginchat.discord.deliverer.ServerChatMessageDeliverer;
import org.bukkit.Bukkit;

@RequiredArgsConstructor
public class DiscordHandler {

  private final RyuZUPluginChat plugin;
  private final String token;

  private DiscordClient client;
  private GatewayDiscordClient gateway;

  private DiscordMessageDeliverer discordMessageDeliverer;
  private ServerChatMessageDeliverer serverChatMessageDeliverer;

  public boolean init() {

    client = DiscordClient.create(token);
    gateway = client.login().block();

    if (gateway == null) {
      return false;
    }

    discordMessageDeliverer = new DiscordMessageDeliverer(plugin);
    serverChatMessageDeliverer = new ServerChatMessageDeliverer(plugin, client);

    // TODO これ要らなくない？
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

                discordMessageDeliverer.sendToGlobal(event);
              } catch (Exception e) {
                e.printStackTrace();
              }
            });
  }

  private void registerDiscordToChannels(Snowflake discordChannelId, ChannelChatSyncData syncData) {
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

                discordMessageDeliverer.sendToChannel(event, syncData);
              } catch (Exception e) {
                e.printStackTrace();
              }
            });
  }

  private void registerGlobalToDiscord(Snowflake chId, boolean vcMode) {
    RestChannel targetChannel = client.getChannelById(chId);
    plugin
        .getSubscriber()
        .registerPublicConsumer(
            (data) -> {
              if (data.isFromDiscord()) {
                return;
              }

              Bukkit.getScheduler()
                  .runTaskAsynchronously(
                      plugin,
                      () -> serverChatMessageDeliverer.sendToDiscord(data, targetChannel, vcMode));
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

              Bukkit.getScheduler()
                  .runTaskAsynchronously(
                      plugin,
                      () -> serverChatMessageDeliverer.sendToDiscord(data, targetChannel, vcMode));
            });
  }

  private void registerPrivateToDiscord(Snowflake discordChannelId, boolean vcMode) {
    RestChannel targetChannel = client.getChannelById(discordChannelId);
    plugin
        .getSubscriber()
        .registerTellConsumer(
            (data) ->
                Bukkit.getScheduler()
                    .runTaskAsynchronously(
                        plugin,
                        () ->
                            serverChatMessageDeliverer.sendtoDiscord(data, targetChannel, vcMode)));
  }

  public void disconnect() {
    if (gateway != null) {
      gateway.logout().block();
    }
  }
}
