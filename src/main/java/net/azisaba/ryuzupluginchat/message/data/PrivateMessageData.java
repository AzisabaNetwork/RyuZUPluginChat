package net.azisaba.ryuzupluginchat.message.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PrivateMessageData implements MessageData {

  private long id;

  private String format;
  private String sendServerName;
  private String receiveServerName;
  private String sentPlayerName;
  private String sentPlayerDisplayName;
  private UUID sentPlayerUuid;
  private String receivedPlayerName;
  private String receivedPlayerDisplayName;
  private UUID receivedPlayerUUID;

  private boolean japanized;
  private String preReplaceMessage;

  private String message;

  private boolean delivered = false;

  public String format() {
    // format が存在する場合はそれを使用し、ない場合は空白
    String defaultFormat = format != null ? format : "";

    // receivedPlayerNameが存在する場合はそれにし、targetPlayerが存在する場合は名前を取得し、ない場合はUUIDで置き換え
    String formattedReceivedPlayerName;
    if (this.receivedPlayerName != null) {
      formattedReceivedPlayerName = this.receivedPlayerName;
    } else {
      Player targetPlayer = Bukkit.getPlayer(receivedPlayerUUID);
      formattedReceivedPlayerName =
          targetPlayer != null ? targetPlayer.getName() : receivedPlayerUUID.toString();
    }

    // DisplayNameが存在する場合はそれを、存在しない場合はPlayerNameを使用する
    String receivedDisplayName =
        receivedPlayerDisplayName != null ? receivedPlayerDisplayName : formattedReceivedPlayerName;
    String sentDisplayName = sentPlayerDisplayName != null ? sentPlayerDisplayName : sentPlayerName;

    String formatted =
        defaultFormat
            .replace("[SendServerName]", convertEmptyIfNull(sendServerName))
            .replace("[ReceiveServerName]", convertEmptyIfNull(receiveServerName))
            .replace("[PlayerName]", convertEmptyIfNull(sentPlayerName))
            .replace("[ReceivePlayerName]", convertEmptyIfNull(formattedReceivedPlayerName))
            .replace("[PlayerDisplayName]", convertEmptyIfNull(sentDisplayName))
            .replace("[ReceivePlayerDisplayName]", convertEmptyIfNull(receivedDisplayName));

    formatted = GlobalMessageData.LEGACY_SERIALIZER.serialize(GlobalMessageData.LEGACY_SERIALIZER.deserialize(formatted));
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
