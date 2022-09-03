package net.azisaba.ryuzupluginchat.message.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.azisaba.ryuzupluginchat.util.Chat;
import org.bukkit.ChatColor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GlobalMessageData implements MessageData {

  private String format;
  private String lunaChatPrefix; // TODO: remove (存在しない)
  private String luckPermsPrefix;
  private String ryuzuMapPrefix;
  private String sendServerName;
  private String receiveServerName;
  private UUID playerUuid;
  private String playerName;
  private String playerDisplayName;
  private String ryuzuMapSuffix;
  private String lunaChatSuffix; // TODO: remove (存在しない)
  private String luckPermsSuffix;

  private boolean japanized;
  private String preReplaceMessage;

  private boolean fromDiscord;

  private String message;

  public String format() {
    // format が存在する場合はそれを使用し、ない場合は空白
    String defaultFormat = format != null ? format : "";
    // DisplayName が存在する場合はそれを使用し、ない場合はPlayerName
    String formattedPlayerName = playerDisplayName != null ? playerDisplayName : playerName;

    if (fromDiscord) {
      defaultFormat = Chat.f("&r[&9Discord&r]{0}", defaultFormat);
    }

    String formatted =
        defaultFormat
            .replace("[LuckPermsPrefix]", convertEmptyIfNull(luckPermsPrefix))
            .replace("[LunaChatPrefix]", convertEmptyIfNull(lunaChatPrefix))
            .replace("[RyuZUMapPrefix]", convertEmptyIfNull(ryuzuMapPrefix))
            .replace("[SendServerName]", convertEmptyIfNull(sendServerName))
            .replace("[ReceiveServerName]", convertEmptyIfNull(receiveServerName))
            .replace("[PlayerName]", convertEmptyIfNull(formattedPlayerName))
            .replace("[RyuZUMapSuffix]", convertEmptyIfNull(ryuzuMapSuffix))
            .replace("[LunaChatSuffix]", convertEmptyIfNull(lunaChatSuffix))
            .replace("[LuckPermsSuffix]", convertEmptyIfNull(luckPermsSuffix));

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
