package net.azisaba.ryuzupluginchat.discord;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.azisaba.ryuzupluginchat.RyuZUPluginChat;
import net.azisaba.ryuzupluginchat.discord.data.ChannelChatSyncData;
import net.azisaba.ryuzupluginchat.discord.data.GlobalChatSyncData;
import net.azisaba.ryuzupluginchat.discord.data.PrivateChatSyncData;
import net.azisaba.ryuzupluginchat.discord.deliverer.DiscordMessageDeliverer;
import net.azisaba.ryuzupluginchat.discord.deliverer.ServerChatMessageDeliverer;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.bukkit.Bukkit;

@RequiredArgsConstructor
public class DiscordHandler extends ListenerAdapter {

  private final RyuZUPluginChat plugin;

  private JDA jda;
  private volatile DiscordMessageDeliverer discordMessageDeliverer;
  private volatile ServerChatMessageDeliverer serverChatMessageDeliverer;

  // Maps a Discord channel id to the sync settings (Global and/or Channel) attached to it.
  private final Map<Long, DiscordInputConfiguration> channelConfigurations = new ConcurrentHashMap<>();

  public boolean init(String token) {
    try {
      jda = JDABuilder.createDefault(token)
          .enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT)
          .addEventListeners(this)
          .build();

      // Construct the deliverers before awaiting the ready status, so that they
      // are available as soon as gateway events start arriving.
      this.discordMessageDeliverer = new DiscordMessageDeliverer(plugin);
      this.serverChatMessageDeliverer = new ServerChatMessageDeliverer(plugin, jda);

      jda.awaitReady();

      return true;
    } catch (Exception e) {
      plugin.getSLF4JLogger().error("Failed to initialize the Discord bot", e);
      return false;
    }
  }

  // Discord => Minecraft
  @Override
  public void onMessageReceived(MessageReceivedEvent event) {
    if (event.isWebhookMessage() || event.getAuthor().isBot()) {
      return;
    }

    DiscordInputConfiguration configuration =
        channelConfigurations.get(event.getChannel().getIdLong());
    if (configuration == null) {
      return;
    }

    if (configuration.getGlobalData().isDiscordInputEnabled()) {
      this.discordMessageDeliverer.sendToGlobal(event);
    }
    if (configuration.getChannelData().isDiscordInputEnabled()) {
      this.discordMessageDeliverer.sendToChannel(event, configuration.getChannelData());
    }
  }

  public void connectUsing(DiscordMessageConnection connectionData) {
    long channelId = connectionData.getDiscordChannelId();

    GlobalChatSyncData globalData = connectionData.getGlobalChatSyncData();
    ChannelChatSyncData channelData = connectionData.getChannelChatSyncData();
    PrivateChatSyncData privateData = connectionData.getPrivateChatSyncData();

    if (globalData.isEnabled()) {
      registerGlobalToDiscord(channelId, globalData.isVoiceChatMode());
    }

    if (channelData.isEnabled()) {
      registerLunaChatChannelToDiscord(channelData, channelId, channelData.isVoiceChatMode());
    }

    if (privateData.isEnabled()) {
      registerPrivateToDiscord(channelId, privateData.isVoiceChatMode());
    }

    // Global and Channel inputs are not mutually exclusive, both may be attached
    // to the same Discord channel, just like the previous Discord4J implementation.
    if (globalData.isDiscordInputEnabled() || channelData.isDiscordInputEnabled()) {
      channelConfigurations.put(channelId, new DiscordInputConfiguration(globalData, channelData));
    }
  }

  private void registerGlobalToDiscord(long chId, boolean vcMode) {
    MessageChannel targetChannel = lookupTextChannel(chId);
    if (targetChannel == null) {
      return;
    }

    plugin.getSubscriber().registerPublicConsumer((data) -> {
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
      ChannelChatSyncData channelChatSyncData, long chId, boolean vcMode) {
    MessageChannel targetChannel = lookupTextChannel(chId);
    if (targetChannel == null) {
      return;
    }

    plugin.getSubscriber().registerChannelChatConsumer((data) -> {
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

  private void registerPrivateToDiscord(long chId, boolean vcMode) {
    MessageChannel targetChannel = lookupTextChannel(chId);
    if (targetChannel == null) {
      return;
    }

    plugin.getSubscriber().registerTellConsumer((data) ->
        Bukkit.getScheduler()
            .runTaskAsynchronously(
                plugin,
                () -> serverChatMessageDeliverer.sendToDiscord(data, targetChannel, vcMode)));
  }

  private MessageChannel lookupTextChannel(long chId) {
    MessageChannel targetChannel = jda.getTextChannelById(chId);
    if (targetChannel == null) {
      plugin.getLogger()
          .warning(
              "Failed to find the Discord text channel (id: " + chId + "). "
                  + "Make sure the id is correct and the bot can see that channel.");
    }
    return targetChannel;
  }

  public void disconnect() {
    if (jda == null) {
      return;
    }

    jda.shutdown();
    try {
      // Flush the remaining queued requests, but give up after 10 seconds.
      if (!jda.awaitShutdown(10, TimeUnit.SECONDS)) {
        jda.shutdownNow();
        jda.awaitShutdown();
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      jda.shutdownNow();
    }
  }

  // Both sync settings are never null; check isDiscordInputEnabled() to know
  // whether the bot should read messages from the Discord channel.
  @RequiredArgsConstructor
  @Getter
  private static class DiscordInputConfiguration {
    private final GlobalChatSyncData globalData;
    private final ChannelChatSyncData channelData;
  }
}
