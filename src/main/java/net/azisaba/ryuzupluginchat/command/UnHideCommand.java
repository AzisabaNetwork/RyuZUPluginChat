package net.azisaba.ryuzupluginchat.command;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import net.azisaba.ryuzupluginchat.RyuZUPluginChat;
import net.azisaba.ryuzupluginchat.util.Chat;
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
      return true;
    }
    Player p = (Player) sender;

    if (args.length == 0) {
      p.sendMessage(Chat.f("&c使い方: /{0} <プレイヤー>", label));
      return true;
    }

    UUID uuid = plugin.getPlayerUUIDMapContainer().getUUID(args[0]);
    if (uuid == null) {
      p.sendMessage(Chat.f("&e{0} &cという名前のプレイヤーが見つかりませんでした。", args[0]));
      return true;
    }
    if (uuid.equals(p.getUniqueId())) {
      p.sendMessage(Chat.f("&c自分自身のチャットの非表示設定を編集することはできません！"));
      return true;
    }

    if (!plugin.getHideInfoController().isHidingPlayer(p.getUniqueId(), uuid)) {
      p.sendMessage(Chat.f("&cあなたはまだ{0}のチャットを非表示にしていません\n&e/hide {0} &cで非表示設定が可能です", args[0]));
      return true;
    }

    plugin.getHideInfoController().removeHide(p.getUniqueId(), uuid);
    p.sendMessage(Chat.f("&a{0}のチャットの非表示を解除しました", args[0]));
    return true;
  }

  @Override
  public @Nullable List<String> onTabComplete(
      @NotNull CommandSender sender,
      @NotNull Command command,
      @NotNull String alias,
      @NotNull String[] args) {

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
