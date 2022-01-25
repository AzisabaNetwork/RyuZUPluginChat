package ryuzupluginchat.ryuzupluginchat.discord;

import discord4j.common.util.Snowflake;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ryuzupluginchat.ryuzupluginchat.discord.data.ChannelChatSyncData;
import ryuzupluginchat.ryuzupluginchat.discord.data.GlobalChatSyncData;
import ryuzupluginchat.ryuzupluginchat.discord.data.PrivateChatSyncData;

@Getter
@RequiredArgsConstructor
public class DiscordMessageConnection {

  private final String id;

  private final Snowflake discordChannelId;

  private final GlobalChatSyncData globalChatSyncData;
  private final ChannelChatSyncData channelChatSyncData;
  private final PrivateChatSyncData privateChatSyncData;

}
