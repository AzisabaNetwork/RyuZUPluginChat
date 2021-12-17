package ryuzupluginchat.ryuzupluginchat.util.message;

import lombok.RequiredArgsConstructor;
import org.bukkit.ChatColor;
import ryuzupluginchat.ryuzupluginchat.util.ColorUtils;

@RequiredArgsConstructor
public class GlobalMessageData {

  private final String format;
  private final String lunaChatPrefix; // TODO: remove (存在しない)
  private final String luckPermsPrefix;
  private final String ryuzuMapPrefix;
  private final String sendServerName;
  private final String receiveServerName;
  private final String playerName;
  private final String playerDisplayName;
  private final String ryuzuMapSuffix;
  private final String lunaChatSuffix; // TODO: remove (存在しない)
  private final String luckPermsSuffix;

  private final boolean japanized;
  private final String preReplaceMessage;

  private final boolean fromDiscord;

  private final String message;

  public String format() {
    // format が存在する場合はそれを使用し、ない場合は空白
    String defaultFormat = format != null ? format : "";
    // DisplayName が存在する場合はそれを使用し、ない場合はPlayerName
    String formattedPlayerName = playerDisplayName != null ? playerDisplayName : playerName;

    if (fromDiscord) {
      defaultFormat =
          ChatColor.WHITE + "[" + ChatColor.BLUE + "Discord" + ChatColor.WHITE + "]"
              + defaultFormat;
    }

    String formatted = defaultFormat.replace("[LuckPermsPrefix]", luckPermsPrefix)
        .replace("[LunaChatPrefix]", convertEmptyIfNull(lunaChatPrefix))
        .replace("[RyuZUMapPrefix]", convertEmptyIfNull(ryuzuMapPrefix))
        .replace("[SendServerName]", convertEmptyIfNull(sendServerName))
        .replace("[ReceiveServerName]", convertEmptyIfNull(receiveServerName))
        .replace("[PlayerName]", convertEmptyIfNull(formattedPlayerName))
        .replace("[RyuZUMapSuffix]", convertEmptyIfNull(ryuzuMapSuffix))
        .replace("[LunaChatSuffix]", convertEmptyIfNull(lunaChatSuffix))
        .replace("[LuckPermsSuffix]", convertEmptyIfNull(luckPermsSuffix));

    formatted = ColorUtils.setColor(formatted);
    if (japanized) {
      formatted = formatted.replace("[PreReplaceMessage]",
          "(" + convertEmptyIfNull(preReplaceMessage) + ")");
    } else {
      formatted = formatted.replace("[PreReplaceMessage]", "");
    }

    return formatted.replace("[Message]", message);
  }

  private String convertEmptyIfNull(String value) {
    return value != null ? value : "";
  }

}
