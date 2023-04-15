package net.azisaba.ryuzupluginchat.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import net.azisaba.ryuzupluginchat.RyuZUPluginChat;
import net.azisaba.ryuzupluginchat.localization.Messages;
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
      Messages.sendFormatted(sender, "command.error.sender_not_player");
      return true;
    }

    Player p = (Player) sender;
    if (args.length <= 1) {
      Messages.sendFormatted(p, "command.tell.usage", label);
      return true;
    }
    if (args[0].equalsIgnoreCase(p.getName())) {
      Messages.sendFormatted(p, "command.tell.error.cannot_message_self");
      return true;
    }

    RyuZUPluginChat.newChain()
        .asyncFirst(
            () -> {
              UUID targetUUID = plugin.getPlayerUUIDMapContainer().getUUID(args[0]);
              if (targetUUID != null && !plugin.getVanishController().isVanished(targetUUID)) {
                return targetUUID;
              }

              List<String> matchNames = getPlayerNamesStartsWith(args[0], p.getName());

              if (matchNames.isEmpty()) {
                Messages.sendFormatted(sender, "command.error.player_not_found", args[0]);
                return null;
              } else if (matchNames.size() > 1) {
                Messages.sendFormatted(p, "command.tell.error.ambiguous", createColoredPlayerNameList(matchNames));
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
      return Collections.emptyList();
    }

    List<String> list = new ArrayList<>();
    Player p = (Player) sender;
    if (args.length == 1) {
      list.addAll(
          plugin.getPlayerUUIDMapContainer().getAllNames().stream()
              .filter(
                  l -> !l.equals(p.getName()) && l.toLowerCase().startsWith(args[0].toLowerCase()))
              .filter(
                  name -> !plugin.getVanishController().isVanished(plugin.getPlayerUUIDMapContainer().getUUID(name))
              )
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
        .filter(
            ign -> !plugin.getVanishController().isVanished(plugin.getPlayerUUIDMapContainer().getUUID(ign))
        )
        .collect(Collectors.toList());
  }
}
