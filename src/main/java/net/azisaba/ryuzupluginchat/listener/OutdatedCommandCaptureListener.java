package net.azisaba.ryuzupluginchat.listener;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import net.azisaba.ryuzupluginchat.RyuZUPluginChat;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.TabCompleteEvent;

@RequiredArgsConstructor
public class OutdatedCommandCaptureListener implements Listener {

  private final RyuZUPluginChat plugin;

  private final List<String> tellCommands = Arrays.asList("/tell", "/t", "/msg", "/message", "/m");
  private final List<String> redirects =
      Arrays.asList("/tell", "/reply", "/t", "/r", "/msg", "/message", "/m");

  @EventHandler
  public void onCommand(PlayerCommandPreprocessEvent e) {
    String[] labelAndArgs = e.getMessage().split(" ");
    if (labelAndArgs.length == 0) {
      return;
    }
    String label = labelAndArgs[0];

    if (redirects.contains(label.toLowerCase())) {
      labelAndArgs[0] = "/" + plugin.getName().toLowerCase() + ":" + label.substring(1);
    }

    e.setMessage(String.join(" ", labelAndArgs));
  }

  @EventHandler
  public void onTabComplete(TabCompleteEvent e) {
    String[] split = e.getBuffer().split(" ");
    String label = split[0];
    if (!tellCommands.contains(label.toLowerCase())) {
      return;
    }
    if (!(e.getSender() instanceof Player)) {
      return;
    }

    final String target;
    if (split.length > 1) {
      target = split[1];
    } else {
      target = "";
    }

    Player p = (Player) e.getSender();

    e.setCompletions(
        plugin.getPlayerUUIDMapContainer().getAllNames().stream()
            .filter(l -> !l.equals(p.getName()) && l.toLowerCase().startsWith(target.toLowerCase()))
            .filter(name -> !plugin.getVanishController().isVanished(plugin.getPlayerUUIDMapContainer().getUUID(name)))
            .collect(Collectors.toList()));
  }
}
