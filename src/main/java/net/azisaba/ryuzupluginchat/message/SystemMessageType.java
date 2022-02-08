package net.azisaba.ryuzupluginchat.message;

import lombok.Getter;

@Getter
public enum SystemMessageType {

  GLOBAL_SYSTEM_MESSAGE, PRIVATE_SYSTEM_MESSAGE, IMITATION_CHAT, PRIVATE_CHAT_RECEIVED;

  @Override
  public String toString() {
    return name();
  }
}
