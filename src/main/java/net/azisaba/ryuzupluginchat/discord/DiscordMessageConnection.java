package net.azisaba.ryuzupluginchat.discord;

import discord4j.common.util.Snowflake;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.azisaba.ryuzupluginchat.discord.data.GlobalChatSyncData;
import net.azisaba.ryuzupluginchat.discord.data.ChannelChatSyncData;
import net.azisaba.ryuzupluginchat.discord.data.PrivateChatSyncData;

@Getter
@RequiredArgsConstructor
public class DiscordMessageConnection {

  private final String id;

  private final Snowflake discordChannelId;

  private final GlobalChatSyncData globalChatSyncData;
  private final ChannelChatSyncData channelChatSyncData;
  private final PrivateChatSyncData privateChatSyncData;

}
