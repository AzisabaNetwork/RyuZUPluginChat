package ryuzupluginchat.ryuzupluginchat.util;

import lombok.Getter;

@Getter
public enum PublicMessageType {

  GLOBAL_CHAT, PRIVATE_CHAT, PREFIX, SUFFIX, SYSTEM_MESSAGE;

  @Override
  public String toString() {
    return name();
  }
}
