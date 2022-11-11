package net.azisaba.ryuzupluginchat.config;

import discord4j.common.util.Snowflake;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.azisaba.ryuzupluginchat.RyuZUPluginChat;
import net.azisaba.ryuzupluginchat.discord.DiscordMessageConnection;
import net.azisaba.ryuzupluginchat.discord.data.ChannelChatSyncData;
import net.azisaba.ryuzupluginchat.discord.data.GlobalChatSyncData;
import net.azisaba.ryuzupluginchat.discord.data.PrivateChatSyncData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import redis.clients.jedis.HostAndPort;

@Getter
@RequiredArgsConstructor
public class RPCConfig {

  private final RyuZUPluginChat plugin;

  private String serverName;
  private HostAndPort hostAndPort;
  private String redisUserName;
  private String redisPassword;

  private String globalChatFormat;
  private String privateChatFormat;

  private String groupName;

  private boolean discordBotEnabled;
  private String discordBotToken;
  private String vcCommandLunaChatChannel;

  private boolean defaultDisablePrivateChatInspect;

  private final List<DiscordMessageConnection> messageConnections = new ArrayList<>();

  public void load() {
    plugin.saveDefaultConfig();
    FileConfiguration conf = plugin.getConfig();

    serverName = conf.getString("server-name");
    hostAndPort = new HostAndPort(conf.getString("redis.hostname"), conf.getInt("redis.port"));
    redisUserName = conf.getString("redis.user");
    redisPassword = conf.getString("redis.password");

    if (redisUserName != null && redisUserName.equals("")) {
      redisUserName = null;
    }
    if (redisPassword != null && redisPassword.equals("")) {
      redisPassword = null;
    }

    globalChatFormat = conf.getString("formats.global");
    privateChatFormat = conf.getString("formats.private");

    groupName = conf.getString("redis.group");

    discordBotEnabled = conf.getBoolean("discord.enable", false);
    if (!discordBotEnabled) {
      return;
    }

    discordBotToken = Objects.requireNonNull(conf.getString("discord.token", "Token here"));

    if (discordBotToken.equals("Token here")) {
      plugin
          .getLogger()
          .warning("Discord Bot Token is not specified or invalid. " + "Bot has been disabled.");
      discordBotEnabled = false;
    }

    vcCommandLunaChatChannel = conf.getString("discord.vc-command-lunachat-channel", null);

    defaultDisablePrivateChatInspect = conf.getBoolean("default-disable-private-chat-inspect", false);

    ConfigurationSection section = conf.getConfigurationSection("discord.connections");
    if (section == null) {
      return;
    }

    messageConnections.clear();
    for (String id : section.getKeys(false)) {
      DiscordMessageConnection connection =
          importConnectionDataFromConfig(conf, "discord.connections." + id, id);

      if (connection == null) {
        plugin.getLogger().warning("Failed to load 'discord.connections." + id + "' section.");
        continue;
      }

      messageConnections.add(connection);
    }
  }

  public void setGlobalChatFormat(String format) {
    globalChatFormat = format;
    FileConfiguration conf = plugin.getConfig();
    conf.set("formats.global", format);
    plugin.saveConfig();
  }

  public void setPrivateChatFormat(String format) {
    privateChatFormat = format;
    FileConfiguration conf = plugin.getConfig();
    conf.set("formats.private", format);
    plugin.saveConfig();
  }

  public void reloadConfig() {
    plugin.reloadConfig();
    load();
  }

  private DiscordMessageConnection importConnectionDataFromConfig(
      FileConfiguration conf, String section, String id) {
    boolean discordInputDefault = conf.getBoolean(section + ".discord-input", false);
    boolean vcModeDefault = conf.getBoolean(section + ".vc-mode", false);

    long discordChIdLong = conf.getLong(section + ".discord-channel-id", -1L);
    if (discordChIdLong < 0) {
      plugin
          .getLogger()
          .warning("Invalid discord channel id ( " + section + ".discord-channel-id )");
      return null;
    }
    Snowflake discordChannelId = Snowflake.of(discordChIdLong);

    GlobalChatSyncData globalData;
    ChannelChatSyncData channelData;
    PrivateChatSyncData privateData;

    // global
    if (conf.getBoolean(section + ".global.enable", false)) {
      boolean vc = conf.getBoolean(section + ".global.vc-mode", vcModeDefault);
      boolean discordInput =
          conf.getBoolean(section + ".global.discord-input", discordInputDefault);
      globalData = new GlobalChatSyncData(true, vc, discordInput);
    } else {
      globalData = new GlobalChatSyncData(false, false, false);
    }

    // channel
    if (conf.getBoolean(section + ".channel.enable", false)) {
      boolean vc = conf.getBoolean(section + ".channel.vc-mode", vcModeDefault);
      boolean discordInput =
          conf.getBoolean(section + ".channel.discord-input", discordInputDefault);
      List<String> matches = null;
      if (conf.isSet(section + ".channel.matches")) {
        if (conf.isString(section + ".channel.matches")) {
          matches = Collections.singletonList(conf.getString(section + ".channel.matches"));
        } else {
          matches = conf.getStringList(section + ".channel.matches");
        }
      }
      channelData = new ChannelChatSyncData(true, vc, discordInput, matches);
    } else {
      channelData = new ChannelChatSyncData(false, false, false, null);
    }

    // private
    if (conf.getBoolean(section + ".private.enable", false)) {
      boolean vc = conf.getBoolean(section + ".private.vc-mode", vcModeDefault);
      privateData = new PrivateChatSyncData(true, vc);
    } else {
      privateData = new PrivateChatSyncData(false, false);
    }

    return new DiscordMessageConnection(id, discordChannelId, globalData, channelData, privateData);
  }
}
