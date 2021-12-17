package ryuzupluginchat.ryuzupluginchat.util.message;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SystemMessageData {

  private final String sendServerName;
  private final String receiveServerName;

  private final String message;

  public String format() {
    return message.replace("[SendServerName]", convertEmptyIfNull(sendServerName))
        .replace("[ReceiveServerName]", convertEmptyIfNull(receiveServerName));
  }

  private String convertEmptyIfNull(String value) {
    return value != null ? value : "";
  }
}
