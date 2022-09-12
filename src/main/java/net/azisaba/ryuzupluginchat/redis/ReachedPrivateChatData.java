package net.azisaba.ryuzupluginchat.redis;

import lombok.Data;

@Data
public class ReachedPrivateChatData {
  private final long id;
  private final String server;
  private final String receivedPlayerName;
  private final String receivedPlayerDisplayName;
}
