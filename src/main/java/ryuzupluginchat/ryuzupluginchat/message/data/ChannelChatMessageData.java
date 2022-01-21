package ryuzupluginchat.ryuzupluginchat.message.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bukkit.ChatColor;
import ryuzupluginchat.ryuzupluginchat.useful.ColorUtils;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChannelChatMessageData {

  private String lunaChatChannelName;
  private String channelColorCode;
  private String lunaChatChannelFormat;
  private String lunaChatPrefix;
  private String luckPermsPrefix;
  private String ryuzuMapPrefix;
  private String sendServerName;
  private String receiveServerName;
  private String playerName;
  private String playerDisplayName;
  private String ryuzuMapSuffix;
  private String lunaChatSuffix;
  private String luckPermsSuffix;

  private boolean japanized;
  private String preReplaceMessage;

  private boolean fromDiscord;

  private String message;

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
    if (japanized) {
      msg = msg.replace("%premsg", preReplaceMessage);
    } else {
      msg = msg.replace("%premsg", "");
    }
    return msg.replace("%msg", message);
  }

  private String getAllPrefixes() {
    if (fromDiscord) {
      return ChatColor.WHITE + "[" + ChatColor.BLUE + "Discord" + ChatColor.WHITE + "]"
          + ChatColor.RESET;
    }
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
