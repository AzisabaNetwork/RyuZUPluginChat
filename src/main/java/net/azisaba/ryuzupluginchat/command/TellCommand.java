package net.azisaba.ryuzupluginchat.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import net.azisaba.ryuzupluginchat.RyuZUPluginChat;
import net.azisaba.ryuzupluginchat.util.ArgsConnectUtils;
import net.azisaba.ryuzupluginchat.util.Chat;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@RequiredArgsConstructor
public class TellCommand implements CommandExecutor, TabCompleter {

  private final RyuZUPluginChat plugin;

  @Override
  public boolean onCommand(
      @NotNull CommandSender sender,
      @NotNull Command command,
      @NotNull String label,
      @NotNull String[] args) {
    if (!(sender instanceof Player)) {
      sender.sendMessage(Chat.f("&cこのコマンドはプレイヤーのみ実行可能です！"));
      return true;
    }

    Player p = (Player) sender;
    if (args.length <= 1) {
      p.sendMessage(Chat.f("&c/{0} [MCID] [Message]", label));
      return true;
    }
    if (args[0].equalsIgnoreCase(p.getName())) {
      p.sendMessage(Chat.f("&c自分にプライベートメッセージを送ることはできません"));
      return true;
    }

    RyuZUPluginChat.newChain()
        .asyncFirst(
            () -> {
              UUID targetUUID = plugin.getPlayerUUIDMapContainer().getUUID(args[0]);
              if (targetUUID != null) {
                return targetUUID;
              }

              List<String> matchNames = getPlayerNamesStartsWith(args[0], p.getName());

              if (matchNames.isEmpty()) {
                p.sendMessage(Chat.f("&e{0}&cというプレイヤーが見つかりませんでした", args[0]));
                return null;
              } else if (matchNames.size() > 1) {
                p.sendMessage(
                    Chat.f(
                        "&c複数プレイヤーが該当するため宛先が絞り込めません {0}", createColoredPlayerNameList(matchNames)));
                return null;
              }

              return plugin.getPlayerUUIDMapContainer().getUUID(matchNames.get(0));
            })
        .abortIfNull()
        .async(
            (uuid) -> {
              assert uuid != null;
              String msg = ArgsConnectUtils.connect(args, 1);
              return plugin.getMessageDataFactory().createPrivateMessageData(p, uuid, msg);
            })
        .sync(
            (data) -> {
              plugin.getPrivateChatResponseWaiter().register(data.getId(), data, 5000L);
              return data;
            })
        .asyncLast((data) -> plugin.getPublisher().publishPrivateMessage(data))
        .execute();
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

  private String createColoredPlayerNameList(List<String> playerNames) {
    return Chat.f("&7( &e{0} &7)", String.join(Chat.f("&7, &e"), playerNames));
  }

  private List<String> getPlayerNamesStartsWith(String name, String... excepts) {
    List<String> exceptsList =
        Arrays.stream(excepts)
            .map(str -> str.toLowerCase(Locale.ROOT))
            .collect(Collectors.toList());

    return plugin.getPlayerUUIDMapContainer().getAllNames().stream()
        .filter(
            mcid -> {
              if (exceptsList.contains(mcid.toLowerCase(Locale.ROOT))) {
                return false;
              }
              return mcid.toLowerCase(Locale.ROOT).startsWith(name.toLowerCase(Locale.ROOT));
            })
        .collect(Collectors.toList());
  }
}
