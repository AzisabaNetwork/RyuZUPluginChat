package net.azisaba.ryuzupluginchat.command;

import lombok.RequiredArgsConstructor;
import net.azisaba.ryuzupluginchat.RyuZUPluginChat;
import net.azisaba.ryuzupluginchat.localization.Messages;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor
public class HideListCommand implements CommandExecutor, TabCompleter {

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

    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
      Map<UUID, String> map = new HashMap<>();
      Set<UUID> hiddenPlayers = plugin.getHideInfoController().getHiddenPlayersBy(p.getUniqueId());
      for (UUID uuid : hiddenPlayers) {
        map.put(uuid, plugin.getPlayerUUIDMapContainer().getNameFromUUID(uuid));
      }
      Bukkit.getScheduler().runTask(plugin, () -> {
        Messages.sendFormatted(p, "command.hidelist.header");
        hiddenPlayers.forEach(uuid -> {
          String name = map.get(uuid);
          if (name == null) {
            return; // should not happen
          }
          TextComponent text = new TextComponent("- ");
          TextComponent textName = new TextComponent(name);
          textName.setColor(ChatColor.GOLD);
          textName.setHoverEvent(
              new HoverEvent(
                  HoverEvent.Action.SHOW_TEXT,
                  new BaseComponent[] {
                      new TextComponent(Messages.getFormattedPlainText(p, "command.hidelist.tooltip", name))
                  })
          );
          textName.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/unhide " + name));
          text.addExtra(textName);
          p.spigot().sendMessage(text);
        });
      });
    });
    return true;
  }

  @Override
  public @Nullable List<String> onTabComplete(
      @NotNull CommandSender sender,
      @NotNull Command command,
      @NotNull String alias,
      @NotNull String[] args) {
    return Collections.emptyList();
  }
}
