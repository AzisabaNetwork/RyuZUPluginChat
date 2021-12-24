package ryuzupluginchat.ryuzupluginchat.message.data;

import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class SystemMessageData {

  private final String sendServerName;
  private final String receiveServerName;

  private final Map<String, Object> map;

  public String format(String msg) {
    return msg
        .replace("[SendServerName]", convertEmptyIfNull(sendServerName))
        .replace("[ReceiveServerName]", convertEmptyIfNull(receiveServerName));
  }

  private String convertEmptyIfNull(String value) {
    return value != null ? value : "";
  }
}
