package net.azisaba.ryuzupluginchat.message.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PrivateMessageData {

  private long id;

  private String format;
  private String sendServerName;
  private String receiveServerName;
  private String sentPlayerName;
  private UUID sentPlayerUuid;
  private String receivedPlayerName;
  private UUID receivedPlayerUUID;

  private boolean japanized;
  private String preReplaceMessage;

  private String message;

  public String format() {
    // format が存在する場合はそれを使用し、ない場合は空白
    String defaultFormat = format != null ? format : "";

    // receivedPlayerNameが存在する場合はそれにし、targetPlayerが存在する場合は名前を取得し、ない場合はUUIDで置き換え
    String receivedPlayerName;
    if (this.receivedPlayerName != null) {
      receivedPlayerName = this.receivedPlayerName;
    } else {
      Player targetPlayer = Bukkit.getPlayer(receivedPlayerUUID);
      receivedPlayerName =
          targetPlayer != null ? targetPlayer.getName() : receivedPlayerUUID.toString();
    }

    String formatted =
        defaultFormat
            .replace("[SendServerName]", convertEmptyIfNull(sendServerName))
            .replace("[ReceiveServerName]", convertEmptyIfNull(receiveServerName))
            .replace("[PlayerName]", convertEmptyIfNull(sentPlayerName))
            .replace("[ReceivePlayerName]", convertEmptyIfNull(receivedPlayerName));

    formatted = ChatColor.translateAlternateColorCodes('&', formatted);
    if (japanized) {
      formatted =
          formatted.replace(
              "[PreReplaceMessage]", "(" + convertEmptyIfNull(preReplaceMessage) + ")");
    } else {
      formatted = formatted.replace("[PreReplaceMessage]", "");
    }

    return formatted.replace("[Message]", message);
  }

  private String convertEmptyIfNull(String value) {
    return value != null ? value : "";
  }
}
