package net.azisaba.ryuzupluginchat.listener;

import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import net.azisaba.ryuzupluginchat.RyuZUPluginChat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

@RequiredArgsConstructor
public class LunaChatHideCommandListener implements Listener {

  private final RyuZUPluginChat plugin;

  private final List<String> chCommandList =
      Arrays.asList(
          "/ch", "/lunachat:ch", "/lc", "/lunachat:lc", "/lunachat", "/lunachat:lunachat");

  @EventHandler
  public void onCommand(PlayerCommandPreprocessEvent e) {
    String message = e.getMessage();
    String[] labelAndArgs = message.split(" ");
    if (labelAndArgs.length < 2 || !chCommandList.contains(labelAndArgs[0].toLowerCase())) {
      return;
    }
    if (!labelAndArgs[1].equalsIgnoreCase("hide")) {
      return;
    }

    Player p = e.getPlayer();

    Bukkit.getScheduler()
        .runTaskLater(
            plugin,
            () ->
                p.sendMessage(
                    ChatColor.RED
                        + "注意: "
                        + ChatColor.YELLOW
                        + "現在LunaChatのプレイヤーhideは機能していません！\n"
                        + ChatColor.YELLOW
                        + "かわりに "
                        + ChatColor.RED
                        + "/hide <プレイヤー> "
                        + ChatColor.YELLOW
                        + "を使用してください！"),
            1L);
  }
}
