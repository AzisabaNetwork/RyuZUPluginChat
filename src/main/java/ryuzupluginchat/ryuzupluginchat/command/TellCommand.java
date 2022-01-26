package ryuzupluginchat.ryuzupluginchat.command;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ryuzupluginchat.ryuzupluginchat.RyuZUPluginChat;
import ryuzupluginchat.ryuzupluginchat.message.data.PrivateMessageData;

@RequiredArgsConstructor
public class TellCommand implements CommandExecutor, TabCompleter {

  private final RyuZUPluginChat plugin;

  @Override
  public boolean onCommand(@NotNull CommandSender sender,
      org.bukkit.command.@NotNull Command command, @NotNull String label, @NotNull String[] args) {
    if (!(sender instanceof Player)) {
      sender.sendMessage(ChatColor.RED + "Only players can use this command.");
      return true;
    }

    Player p = (Player) sender;
    if (args.length <= 1) {
      p.sendMessage(ChatColor.RED + "/" + label + " [MCID] [Message]");
      return true;
    }
    if (args[0].equals(p.getName())) {
      p.sendMessage(ChatColor.RED + "自分にプライベートメッセージを送ることはできません");
      return true;
    }

    UUID targetUUID = plugin.getPlayerUUIDMapContainer().getUUID(args[0]);

    if (targetUUID == null) {

      List<String> matchNames = plugin.getPlayerUUIDMapContainer().getAllNames().stream()
          .filter(name -> !name.equalsIgnoreCase(p.getName())
              && name.toLowerCase().startsWith(args[0].toLowerCase()))
          .collect(Collectors.toList());

      if (matchNames.isEmpty()) {
        p.sendMessage(ChatColor.YELLOW + args[0] + ChatColor.RED + "というプレイヤーが見つかりませんでした");
        return true;
      } else if (matchNames.size() > 1) {
        p.sendMessage(
            ChatColor.RED + "複数プレイヤーが該当するため宛先が絞り込めません " + createColoredPlayerNameList(matchNames));
        return true;
      }

      targetUUID = plugin.getPlayerUUIDMapContainer().getUUID(matchNames.get(0));
    }

    String msg = String.join(" ", args).substring(args[0].length() + 1);
    PrivateMessageData data = plugin.getMessageDataFactory()
        .createPrivateMessageData(p, targetUUID, msg);

    RyuZUPluginChat.newChain()
        .sync(() -> plugin.getPrivateChatResponseWaiter().register(data.getId(), data, 5000L))
        .async(() -> plugin.getPublisher().publishPrivateMessage(data)).execute();
    return true;
  }

  private String createColoredPlayerNameList(List<String> playerNames) {
    return ChatColor.GRAY + "( " + ChatColor.YELLOW + String.join(
        ChatColor.GRAY + ", " + ChatColor.YELLOW, playerNames) + ChatColor.GRAY + " )";
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
