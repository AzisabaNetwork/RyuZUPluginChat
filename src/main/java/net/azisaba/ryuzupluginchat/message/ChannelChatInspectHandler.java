package net.azisaba.ryuzupluginchat.message;

import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@NoArgsConstructor
public class ChannelChatInspectHandler implements InspectHandler {

  private final List<UUID> disablePlayers = new ArrayList<>();

  @Override
  public void setDisable(UUID uuid, boolean silent) {
    if (silent) {
      if (!disablePlayers.contains(uuid)) {
        disablePlayers.add(uuid);
      }
    } else {
      disablePlayers.remove(uuid);
    }
  }

  @Override
  public boolean isDisabled(UUID uuid) {
    return disablePlayers.contains(uuid);
  }
}
