package ryuzupluginchat.ryuzupluginchat.discord.data;

import lombok.Getter;

@Getter
public class PrivateChatSyncData extends ChatSyncData {

  public PrivateChatSyncData(boolean enabled, boolean voiceChatMode) {
    super(enabled, voiceChatMode, false);
  }
}
