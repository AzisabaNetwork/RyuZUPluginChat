package ryuzupluginchat.ryuzupluginchat.util;

import lombok.Getter;

@Getter
public enum SystemMessageType {

  CHAT, PREFIX, SUFFIX, PLAYERS;

  @Override
  public String toString() {
    return name();
  }
}
