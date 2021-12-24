package ryuzupluginchat.ryuzupluginchat.message.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ryuzupluginchat.ryuzupluginchat.useful.ColorUtils;

@Getter
@RequiredArgsConstructor
public class ChannelChatMessageData {

  private final String lunaChatChannelName;
  private final String channelColorCode;
  private final String lunaChatChannelFormat;
  private final String lunaChatPrefix;
  private final String luckPermsPrefix;
  private final String ryuzuMapPrefix;
  private final String sendServerName;
  private final String receiveServerName;
  private final String playerName;
  private final String playerDisplayName;
  private final String ryuzuMapSuffix;
  private final String lunaChatSuffix;
  private final String luckPermsSuffix;

  private final boolean japanized;
  private final String preReplaceMessage;

  private final boolean fromDiscord;

  private final String message;

  public String format() {
    // DisplayName が存在する場合はそれを使用し、ない場合はPlayerName
    String formattedPlayerName = playerDisplayName != null ? playerDisplayName : playerName;

    String msg = lunaChatChannelFormat.replace("%prefix", getAllPrefixes())
        .replace("%suffix", getAllSuffixes())
        .replace("%username", playerName)
        .replace("%displayname", formattedPlayerName)
        .replace("%ch", lunaChatChannelName)
        .replace("%color", channelColorCode);
    msg = ColorUtils.setColor(msg);
    return msg.replace("%msg", msg)
        .replace("%premsg", preReplaceMessage);
  }

  private String getAllPrefixes() {
    return convertEmptyIfNull(luckPermsPrefix) + convertEmptyIfNull(ryuzuMapPrefix)
        + convertEmptyIfNull(lunaChatPrefix);
  }

  private String getAllSuffixes() {
    return convertEmptyIfNull(luckPermsSuffix) + convertEmptyIfNull(ryuzuMapSuffix)
        + convertEmptyIfNull(lunaChatSuffix);
  }

  private String convertEmptyIfNull(String value) {
    return value != null ? value : "";
  }

}
