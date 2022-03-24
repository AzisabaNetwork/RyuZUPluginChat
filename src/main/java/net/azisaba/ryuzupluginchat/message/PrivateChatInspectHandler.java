package net.azisaba.ryuzupluginchat.message;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class PrivateChatInspectHandler {

  private final List<UUID> disablePlayers = new ArrayList<>();

  public void setDisable(UUID uuid, boolean disable) {
    if (disable) {
      if (!disablePlayers.contains(uuid)) {
        disablePlayers.add(uuid);
      }
    } else {
      disablePlayers.remove(uuid);
    }
  }

  public boolean isDisabled(UUID uuid) {
    return disablePlayers.contains(uuid);
  }
}
