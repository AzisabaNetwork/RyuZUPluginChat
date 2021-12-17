package ryuzupluginchat.ryuzupluginchat.util.message;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ryuzupluginchat.ryuzupluginchat.util.ColorUtils;

@Getter
@RequiredArgsConstructor
public class PrivateMessageData {

  private final String format;
  private final String lunaChatPrefix; // TODO: remove (存在しない)
  private final String luckPermsPrefix;
  private final String ryuzuMapPrefix;
  private final String sendServerName;
  private final String receiveServerName;
  private final String sentPlayerName;
  private final String receivedPlayerName;
  private final String ryuzuMapSuffix;
  private final String lunaChatSuffix; // TODO: remove (存在しない)
  private final String luckPermsSuffix;

  private final boolean japanized;
  private final String preReplaceMessage;

  private final String message;

  public String format() {
    // format が存在する場合はそれを使用し、ない場合は空白
    String defaultFormat = format != null ? format : "";

    String formatted = defaultFormat.replace("[LuckPermsPrefix]", luckPermsPrefix)
        .replace("[LunaChatPrefix]", convertEmptyIfNull(lunaChatPrefix))
        .replace("[RyuZUMapPrefix]", convertEmptyIfNull(ryuzuMapPrefix))
        .replace("[SendServerName]", convertEmptyIfNull(sendServerName))
        .replace("[ReceiveServerName]", convertEmptyIfNull(receiveServerName))
        .replace("[PlayerName]", convertEmptyIfNull(sentPlayerName))
        .replace("[ReceivePlayerName]", convertEmptyIfNull(receivedPlayerName))
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
