package net.azisaba.ryuzupluginchat.discord;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.azisaba.ryuzupluginchat.discord.data.ChannelChatSyncData;
import net.azisaba.ryuzupluginchat.discord.data.GlobalChatSyncData;
import net.azisaba.ryuzupluginchat.discord.data.PrivateChatSyncData;

@Getter
@RequiredArgsConstructor
public class JDADiscordMessageConnection {

    private final String id;

    private final long discordChannelId;

    private final GlobalChatSyncData globalChatSyncData;
    private final ChannelChatSyncData channelChatSyncData;
    private final PrivateChatSyncData privateChatSyncData;
}
