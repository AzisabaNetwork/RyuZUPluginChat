package net.azisaba.ryuzupluginchat.command;

import lombok.RequiredArgsConstructor;
import net.azisaba.ryuzupluginchat.RyuZUPluginChat;
import net.azisaba.ryuzupluginchat.util.Chat;
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

  @SuppressWarnings("deprecation") // Unfortunately, Adventure API is not implemented in Paper 1.15.2.
  @Override
  public boolean onCommand(
      @NotNull CommandSender sender,
      @NotNull Command cmd,
      @NotNull String label,
      @NotNull String[] args) {
    if (!(sender instanceof Player)) {
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
        p.sendMessage(Chat.f("&eHideしているプレイヤー一覧:"));
        hiddenPlayers.forEach(uuid -> {
          String name = map.get(uuid);
          if (name == null) {
            return; // should not happen
          }
          TextComponent text = new TextComponent("- ");
          TextComponent textName = new TextComponent(name);
          textName.setColor(ChatColor.GOLD);
          textName.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{ new TextComponent("クリックで" + name + "の非表示設定を解除") }));
          textName.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/unhide " + name));
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
