package ryuzupluginchat.ryuzupluginchat.listener;

import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import ryuzupluginchat.ryuzupluginchat.RyuZUPluginChat;

@RequiredArgsConstructor
public class JoinQuitListener implements Listener {

  private final RyuZUPluginChat plugin;

  @EventHandler
  public void onJoin(PlayerJoinEvent e) {
    Player p = e.getPlayer();

    RyuZUPluginChat.newChain()
        .async(() -> plugin.getPlayerUUIDMapContainer().register(p))
        .execute();
  }

  @EventHandler
  public void onQuit(PlayerQuitEvent e) {
    Player p = e.getPlayer();

    RyuZUPluginChat.newChain()
        .async(() -> plugin.getPlayerUUIDMapContainer().unregister(p))
        .execute();
  }
}
