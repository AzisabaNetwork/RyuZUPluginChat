package net.azisaba.ryuzupluginchat.message.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.azisaba.ryuzupluginchat.util.Chat;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChannelChatMessageData implements MessageData {

  private String lunaChatChannelName;
  private String channelColorCode;
  private String lunaChatChannelFormat;
  private String lunaChatPrefix;
  private String luckPermsPrefix;
  private String ryuzuMapPrefix;
  private String sendServerName;
  private String receiveServerName;
  private UUID playerUuid;
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

    String msg =
        lunaChatChannelFormat
            .replace("%prefix", convertEmptyIfNull(getAllPrefixes()))
            .replace("%suffix", convertEmptyIfNull(getAllSuffixes()))
            .replace("%username", convertEmptyIfNull(formattedPlayerName))
            .replace("%displayname", convertEmptyIfNull(formattedPlayerName))
            .replace("%ch", convertEmptyIfNull(lunaChatChannelName))
            .replace("%color", convertEmptyIfNull(channelColorCode))
            .replace("%servername", convertEmptyIfNull(sendServerName));
    msg = GlobalMessageData.LEGACY_SERIALIZER.serialize(GlobalMessageData.LEGACY_SERIALIZER.deserialize(msg));
    if (japanized) {
      msg = msg.replace("%premsg", preReplaceMessage);
    } else {
      msg = msg.replace("%premsg", "");
    }
    return msg.replace("%msg", message);
  }

  private String getAllPrefixes() {
    if (fromDiscord) {
      return Chat.f("&r[&9Discord&r]");
    }
    return convertEmptyIfNull(luckPermsPrefix)
        + convertEmptyIfNull(ryuzuMapPrefix)
        + convertEmptyIfNull(lunaChatPrefix);
  }

  private String getAllSuffixes() {
    return convertEmptyIfNull(luckPermsSuffix)
        + convertEmptyIfNull(ryuzuMapSuffix)
        + convertEmptyIfNull(lunaChatSuffix);
  }

  private String convertEmptyIfNull(String value) {
    return value != null ? value : "";
  }
}
