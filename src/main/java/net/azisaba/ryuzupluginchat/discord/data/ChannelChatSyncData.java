package net.azisaba.ryuzupluginchat.discord.data;

import java.util.List;
import lombok.Getter;

@Getter
public class ChannelChatSyncData extends ChatSyncData {

  private final List<String> matches;

  public ChannelChatSyncData(
      boolean enabled, boolean voiceChatMode, boolean discordInputEnabled, List<String> matches) {
    super(enabled, voiceChatMode, discordInputEnabled);
    this.matches = matches;
  }

  public boolean isMatch(String channelName) {
    if (matches == null) {
      return true;
    }
    for (String str : matches) {
      String regex = ("\\Q" + str + "\\E").replace("*", "\\E.*\\Q");
      if (channelName.matches(regex)) {
        return true;
      }
    }
    return false;
  }
}
