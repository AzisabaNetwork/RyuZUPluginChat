package net.azisaba.ryuzupluginchat.listener;

import lombok.RequiredArgsConstructor;
import net.azisaba.ryuzupluginchat.RyuZUPluginChat;
import net.azisaba.ryuzupluginchat.event.AsyncGlobalMessageEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

@RequiredArgsConstructor
public class HideAllListener implements Listener {

  private final RyuZUPluginChat plugin;

  @EventHandler
  public void onGlobalMessage(AsyncGlobalMessageEvent e) {
    e.getRecipients()
        .removeIf(p -> plugin.getHideAllInfoController().isHideAllPlayer(p.getUniqueId()));
  }
}
