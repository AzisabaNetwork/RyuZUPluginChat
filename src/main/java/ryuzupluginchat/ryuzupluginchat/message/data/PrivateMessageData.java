package ryuzupluginchat.ryuzupluginchat.message.data;

import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ryuzupluginchat.ryuzupluginchat.useful.ColorUtils;

@Getter
@RequiredArgsConstructor
public class PrivateMessageData {

  private final String format;
  private final String sendServerName;
  private final String receiveServerName;
  private final String sentPlayerName;
  private final UUID receivedPlayerUUID;

  private final boolean japanized;
  private final String preReplaceMessage;

  private final String message;

  public String format() {
    // format が存在する場合はそれを使用し、ない場合は空白
    String defaultFormat = format != null ? format : "";
    // targetPlayerが存在する場合は名前を取得し、ない場合はUUIDで置き換え
    Player targetPlayer = Bukkit.getPlayer(receivedPlayerUUID);
    String receivedPlayerName =
        targetPlayer != null ? targetPlayer.getName() : receivedPlayerUUID.toString();

    String formatted = defaultFormat
        .replace("[SendServerName]", convertEmptyIfNull(sendServerName))
        .replace("[ReceiveServerName]", convertEmptyIfNull(receiveServerName))
        .replace("[PlayerName]", convertEmptyIfNull(sentPlayerName))
        .replace("[ReceivePlayerName]", convertEmptyIfNull(receivedPlayerName));

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
