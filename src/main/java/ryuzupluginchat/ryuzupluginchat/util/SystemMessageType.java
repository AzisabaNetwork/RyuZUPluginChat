package ryuzupluginchat.ryuzupluginchat.util;

import lombok.Getter;

@Getter
public enum SystemMessageType {

  GLOBAL_SYSTEM_MESSAGE, PRIVATE_SYSTEM_MESSAGE, IMITATION_CHAT;

  @Override
  public String toString() {
    return name();
  }
}
