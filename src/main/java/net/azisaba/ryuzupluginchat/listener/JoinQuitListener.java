package net.azisaba.ryuzupluginchat.listener;

import lombok.RequiredArgsConstructor;
import net.azisaba.ryuzupluginchat.RyuZUPluginChat;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

@RequiredArgsConstructor
public class JoinQuitListener implements Listener {

  private final RyuZUPluginChat plugin;

  @EventHandler
  public void onJoin(PlayerJoinEvent e) {
    Player p = e.getPlayer();

    Bukkit.getScheduler()
        .runTaskLaterAsynchronously(
            plugin,
            () -> {
              if (p.isOnline()) {
                plugin.getPlayerUUIDMapContainer().register(p);
              }
            },
            20L * 3L);

    plugin.getHideAllInfoController().refreshHideAllInfoAsync(p.getUniqueId());

    if (plugin.getRpcConfig().isDefaultDisablePrivateChatInspect()) {
      plugin.getPrivateChatInspectHandler().setDisable(p.getUniqueId(), true);
    }
    if (plugin.getRpcConfig().isDefaultDisableChannelChatInspect()) {
      plugin.getChannelChatInspectHandler().setDisable(p.getUniqueId(), true);
    }
  }

  @EventHandler
  public void onQuit(PlayerQuitEvent e) {
    Player p = e.getPlayer();

    Bukkit.getScheduler()
        .runTaskAsynchronously(plugin, () -> plugin.getPlayerUUIDMapContainer().unregister(p));

    plugin.getHideAllInfoController().discardHideAllInfo(p.getUniqueId());
  }
}
