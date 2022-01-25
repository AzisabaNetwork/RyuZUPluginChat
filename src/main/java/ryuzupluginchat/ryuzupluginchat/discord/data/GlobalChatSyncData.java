package ryuzupluginchat.ryuzupluginchat.discord.data;

import lombok.Getter;

@Getter
public class GlobalChatSyncData extends ChatSyncData {

  public GlobalChatSyncData(boolean enabled, boolean voiceChatMode, boolean discordInputEnabled) {
    super(enabled, voiceChatMode, discordInputEnabled);
  }

}
