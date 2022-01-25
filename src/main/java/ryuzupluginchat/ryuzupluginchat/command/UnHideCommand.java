package ryuzupluginchat.ryuzupluginchat.command;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ryuzupluginchat.ryuzupluginchat.RyuZUPluginChat;

@RequiredArgsConstructor
public class UnHideCommand implements CommandExecutor, TabCompleter {

  private final RyuZUPluginChat plugin;

  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd,
      @NotNull String label, @NotNull String[] args) {
    if (!(sender instanceof Player)) {
      return true;
    }
    Player p = (Player) sender;

    if (args.length <= 0) {
      p.sendMessage(ChatColor.RED + "使い方: /" + label + " <プレイヤー>");
      return true;
    }

    UUID uuid = plugin.getPlayerUUIDMapContainer().getUUID(args[0]);
    if (uuid == null) {
      p.sendMessage(ChatColor.YELLOW + args[0] + ChatColor.RED + " という名前のプレイヤーが見つかりませんでした。");
      return true;
    }
    if (uuid.equals(p.getUniqueId())) {
      p.sendMessage(ChatColor.RED + "自分自身のチャットの非表示設定を編集することはできません！");
      return true;
    }

    if (!plugin.getHideInfoController().isHidingPlayer(p.getUniqueId(), uuid)) {
      p.sendMessage(ChatColor.RED + "あなたはまだ" + args[0] + "のチャットを非表示にしていません" + "\n"
          + ChatColor.YELLOW + "/hide " + args[0] + ChatColor.RED + " で非表示設定が可能です");
      return true;
    }

    plugin.getHideInfoController().removeHide(p.getUniqueId(), uuid);
    p.sendMessage(ChatColor.GREEN + args[0] + "のチャットの非表示を解除しました");
    return true;
  }

  @Override
  public @Nullable List<String> onTabComplete(@NotNull CommandSender sender,
      @NotNull Command command, @NotNull String alias, @NotNull String[] args) {

    if (!(sender instanceof Player)) {
      return null;
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
