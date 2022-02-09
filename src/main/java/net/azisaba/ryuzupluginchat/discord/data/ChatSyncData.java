package net.azisaba.ryuzupluginchat.discord.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public abstract class ChatSyncData {

  private final boolean enabled;
  private final boolean voiceChatMode;
  private final boolean discordInputEnabled;
}
