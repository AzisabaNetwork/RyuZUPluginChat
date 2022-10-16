package net.azisaba.ryuzupluginchat.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import net.azisaba.ryuzupluginchat.RyuZUPluginChat;
import net.azisaba.ryuzupluginchat.localization.Messages;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@RequiredArgsConstructor
public class UnHideCommand implements CommandExecutor, TabCompleter {

  private final RyuZUPluginChat plugin;

  @Override
  public boolean onCommand(
      @NotNull CommandSender sender,
      @NotNull Command cmd,
      @NotNull String label,
      @NotNull String[] args) {
    if (!(sender instanceof Player)) {
      Messages.sendFormatted(sender, "command.error.sender_not_player");
      return true;
    }
    Player p = (Player) sender;

    if (args.length == 0) {
      Messages.sendFormatted(p, "command.unhide.usage", label);
      return true;
    }

    UUID uuid = plugin.getPlayerUUIDMapContainer().getUUID(args[0]);
    if (uuid == null) {
      Messages.sendFormatted(sender, "command.error.player_not_found", args[0]);
      return true;
    }
    if (uuid.equals(p.getUniqueId())) {
      // intentionally using hide, because it's impossible to unhide when they can't hide themselves
      Messages.sendFormatted(p, "command.hide.error.cannot_hide_self");
      return true;
    }

    if (!plugin.getHideInfoController().isHidingPlayer(p.getUniqueId(), uuid)) {
      Messages.sendFormatted(p, "command.unhide.error.not_hidden", args[0]);
      return true;
    }

    plugin.getHideInfoController().removeHide(p.getUniqueId(), uuid);
    Messages.sendFormatted(p, "command.unhide.success", args[0]);
    return true;
  }

  @Override
  public @Nullable List<String> onTabComplete(
      @NotNull CommandSender sender,
      @NotNull Command command,
      @NotNull String alias,
      @NotNull String[] args) {

    if (!(sender instanceof Player)) {
      return Collections.emptyList();
    }

    List<String> list = new ArrayList<>();
    Player p = (Player) sender;
    if (args.length == 1) {
      list.addAll(
          plugin.getPlayerUUIDMapContainer().getAllNames().stream()
              .filter(
                  l -> !l.equals(p.getName()) && l.toLowerCase().startsWith(args[0].toLowerCase()))
              .collect(Collectors.toList()));
    }
    return list;
  }
}
