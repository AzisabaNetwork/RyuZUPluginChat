package net.azisaba.ryuzupluginchat.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import net.azisaba.ryuzupluginchat.RyuZUPluginChat;
import net.azisaba.ryuzupluginchat.message.data.SystemMessageData;
import net.azisaba.ryuzupluginchat.util.ArgsConnectUtils;
import net.azisaba.ryuzupluginchat.util.Chat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@RequiredArgsConstructor
public class RPCCommand implements CommandExecutor, TabCompleter {

  private final RyuZUPluginChat plugin;

  private final List<String> redirectArgs = Arrays.asList("tell", "reply", "hide", "unhide");

  private final String permissionDeniedMessage = Chat.f("&cぽまえけんげんないやろ");

  @Override
  public boolean onCommand(
      @NotNull CommandSender sender,
      @NotNull Command command,
      @NotNull String label,
      @NotNull String[] args) {

    if (args.length <= 0) {
      sendUsage(sender, label);
      return true;
    }

    if (args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("r")) {
      if (!sender.hasPermission("rpc.op")) {
        sender.sendMessage(permissionDeniedMessage);
        return true;
      }

      sender.sendMessage(Chat.f("&e非同期でリロードを実行しています..."));
      RyuZUPluginChat.newSharedChain("reload")
          .async(
              () -> {
                long start = System.currentTimeMillis();

                plugin.getRpcConfig().reloadConfig();
                plugin.executeFullReload();

                long end = System.currentTimeMillis();
                sender.sendMessage(Chat.f("&a非同期でシステムをリロードしました &7({0}ms)", end - start));
              })
          .execute();
      return true;
    }

    if (redirectArgs.contains(args[0])) {
      String redirectCommandLabel = ArgsConnectUtils.connect(args);
      Bukkit.dispatchCommand(sender, plugin.getName().toLowerCase() + ":" + redirectCommandLabel);
      return true;
    }

    if (args[0].equalsIgnoreCase("silent")) {
      if (!sender.hasPermission("rpc.op")) {
        sender.sendMessage(permissionDeniedMessage);
        return true;
      }

      Player player = (Player) sender;
      boolean silent = !plugin.getPrivateChatInspectHandler().isDisabled(player.getUniqueId());

      if (args.length >= 2) {
        switch (args[1].toLowerCase()) {
          case "on":
          case "yes":
          case "enable":
            silent = true;
            break;
          case "off":
          case "no":
          case "disable":
            silent = false;
            break;
          default:
            player.sendMessage(Chat.f("&e{0}&cは無効な引数です。", args[1]));
            return true;
        }
      }

      if (silent) {
        plugin.getPrivateChatInspectHandler().setDisable(player.getUniqueId(), true);
        player.sendMessage(Chat.f("&e他プレイヤー同士の個人チャットが&c見えない&eようになりました"));
      } else {
        plugin.getPrivateChatInspectHandler().setDisable(player.getUniqueId(), false);
        player.sendMessage(Chat.f("&e他プレイヤー同士の個人チャットが&a見える&eように設定しました"));
      }
      return true;
    }

    if (args[0].equalsIgnoreCase("prefix") || args[0].equalsIgnoreCase("p")) {
      if (!sender.hasPermission("rpc.op")) {
        sender.sendMessage(permissionDeniedMessage);
        return true;
      }
      if (args.length <= 1) {
        sender.sendMessage(Chat.f("/{0} prefix set [MCID] [Prefix]", label));
        return true;
      }

      if (args[1].equalsIgnoreCase("set")) {
        if (args.length <= 3) {
          sender.sendMessage(Chat.f("&c/{0} prefix set [MCID] [Prefix]"));
          return true;
        }
        UUID uuid = plugin.getPlayerUUIDMapContainer().getUUID(args[2]);
        if (uuid == null) {
          sender.sendMessage(Chat.f("&cプレイヤーが見つかりませんでした"));
          return true;
        }
        String prefix = ArgsConnectUtils.connect(args, 3);
        plugin.getPrefixSuffixContainer().setPrefix(uuid, prefix, true);
        sender.sendMessage(
            Chat.f(
                "&e{0}&aのPrefixを &r{1} &aに変更しました",
                args[2], ChatColor.translateAlternateColorCodes('&', prefix)));
        return true;
      }

      sender.sendMessage(Chat.f("/{0} prefix set [MCID] [Prefix]", label));
      return true;
    }

    if (args[0].equalsIgnoreCase("suffix") || args[0].equalsIgnoreCase("s")) {
      if (!sender.hasPermission("rpc.op")) {
        sender.sendMessage(permissionDeniedMessage);
        return true;
      }
      if (args.length <= 1) {
        sender.sendMessage(Chat.f("&c/{0} suffix set [MCID] [Suffix]", label));
        return true;
      }
      if (args[1].equalsIgnoreCase("set")) {
        if (args.length <= 3) {
          sender.sendMessage(Chat.f("&c/{0} suffix set [MCID] [Suffix]", label));
          return true;
        }

        UUID uuid = plugin.getPlayerUUIDMapContainer().getUUID(args[2]);
        if (uuid == null) {
          sender.sendMessage(Chat.f("&cプレイヤーが見つかりませんでした"));
          return true;
        }

        String suffix = ArgsConnectUtils.connect(args, 3);
        plugin.getPrefixSuffixContainer().setSuffix(uuid, suffix, true);
        sender.sendMessage(
            Chat.f(
                "&e{0}&aのSuffixを &r{1} &aに変更しました",
                args[2], ChatColor.translateAlternateColorCodes('&', suffix)));
        return true;
      }

      sender.sendMessage(Chat.f("&c/{0} suffix set [MCID] [Suffix]", label));
      return true;
    }

    if (args[0].equalsIgnoreCase("message") || args[0].equalsIgnoreCase("msg")) {
      if (!sender.hasPermission("rpc.op")) {
        sender.sendMessage(permissionDeniedMessage);
        return true;
      }

      if (args.length <= 2) {
        sender.sendMessage(
            Chat.f(
                "&9/{0} message [message/player/playermessage] [Message]:指定されたメッセージをGroupに送信します",
                label));
        return true;
      }

      String msg = ArgsConnectUtils.connect(args, 2);
      if (args[1].equalsIgnoreCase("message")) {
        SystemMessageData data =
            plugin.getMessageDataFactory().createGeneralSystemChatMessageData(msg);
        plugin.getPublisher().publishSystemMessage(data);
      } else if (args[1].equalsIgnoreCase("player")) {
        msg = msg.substring(args[2].length() + 1);
        UUID uuid = plugin.getPlayerUUIDMapContainer().getUUID(args[2]);
        if (uuid == null) {
          sender.sendMessage(Chat.f("&e{0}&cという名前のプレイヤーが見つかりませんでした", args[2]));
          return true;
        }

        SystemMessageData data =
            plugin.getMessageDataFactory().createPrivateSystemChatMessageData(uuid, msg);
        RyuZUPluginChat.newChain()
            .async(() -> plugin.getPublisher().publishSystemMessage(data))
            .execute();

      } else if (args[1].equalsIgnoreCase("playermessage")) {
        msg = msg.substring(args[2].length() + 1);
        UUID uuid = plugin.getPlayerUUIDMapContainer().getUUID(args[2]);

        if (uuid == null) {
          sender.sendMessage(Chat.f("&e{0}&cという名前のプレイヤーが見つかりませんでした", args[2]));
          return true;
        }

        SystemMessageData data =
            plugin.getMessageDataFactory().createImitationChatMessageData(uuid, msg);

        RyuZUPluginChat.newChain()
            .async(() -> plugin.getPublisher().publishSystemMessage(data))
            .execute();
      }
      return true;
    }

    if (args[0].equalsIgnoreCase("config") || args[0].equalsIgnoreCase("c")) {
      if (!sender.hasPermission("rpc.op")) {
        sender.sendMessage(permissionDeniedMessage);
        return true;
      }
      if (args.length <= 1) {
        sender.sendMessage(
            Chat.f("&9/{0} config format set <global/private> [format]: formatを編集します", label));
        return true;
      }

      if (args[1].equalsIgnoreCase("format")) {
        if (args.length <= 4) {
          sender.sendMessage(
              Chat.f("&9/{0} config format set <global/private> [format]: formatを編集します", label));
          return true;
        }

        if (args[2].equalsIgnoreCase("set")) {
          String format = ArgsConnectUtils.connect(args, 4);

          switch (args[3].toLowerCase()) {
            case "global":
            case "all":
            case "default":
              plugin.getRpcConfig().setGlobalChatFormat(format);
              break;
            case "private":
            case "tell":
            case "reply":
              plugin.getRpcConfig().setPrivateChatFormat(format);
              break;
            default:
              sender.sendMessage(
                  Chat.f(
                      "&9/{0} config format set <global/private/channel> [format]: formatを編集します",
                      label));
              return true;
          }
          sender.sendMessage(Chat.f("&aFormatを編集しました"));
          return true;
        }
        return true;
      }
      sender.sendMessage(
          Chat.f(
              "&9/{0} config format set <global/private/channel> [format]: formatを編集します", label));
      return true;
    }

    sendUsage(sender, label);
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
      if (sender.hasPermission("rpc.op")) {
        list.addAll(Arrays.asList("prefix", "suffix", "message", "config"));
      }
      list.addAll(redirectArgs);
    }
    if (args.length == 2) {
      if (sender.hasPermission("rpc.op")) {
        if (args[0].equals("prefix") || args[0].equals("suffix")) {
          list.add("set");
        }
        if (args[0].equals("config")) {
          list.addAll(Arrays.asList("format", "channelformat", "tellformat", "list", "group"));
        }
        if (args[0].equals("message")) {
          list.addAll(Arrays.asList("message", "player", "playermessage"));
        }
      }
      if (Arrays.asList("tell", "hide", "unhide").contains(args[0])) {
        list.addAll(
            plugin.getPlayerUUIDMapContainer().getAllNames().stream()
                .filter(name -> !name.equalsIgnoreCase(p.getName()))
                .collect(Collectors.toList()));
      }
    }
    if (args.length == 3) {
      if (sender.hasPermission("rpc.op")) {
        if (args[1].equals("format")) {
          list.add("set");
        }
        if (args[1].equals("list")) {
          list.addAll(Arrays.asList("add", "remove"));
        }
        if (args[1].equals("group")) {
          list.add("remove");
        }
        if (Arrays.asList("player", "playermessage").contains(args[1])) {
          list.addAll(plugin.getPlayerUUIDMapContainer().getAllNames());
        }
      }
    }
    return list;
  }

  private void sendUsage(CommandSender sender, String label) {
    sender.sendMessage(Chat.f("&6------------------------使い方------------------------"));
    if (sender.hasPermission("rpc.op")) {
      sender.sendMessage(Chat.f("&9/{0} prefix :Prefixを編集します", label));
      sender.sendMessage(Chat.f("&9/{0} suffix :Suffixを編集します", label));
      sender.sendMessage(Chat.f("&9/{0} config :Configを編集します", label));
      sender.sendMessage(Chat.f("&9/{0} reload :config.ymlをリロードします", label));
      sender.sendMessage(Chat.f("&9/{0} silent :他プレイヤーの個チャが見える機能を変更します", label));
    }
    sender.sendMessage(Chat.f("&9/{0} tell :プレイヤーにプライベートメッセージを送信します", label));
    sender.sendMessage(Chat.f("&9/{0} reply :直近のプライベートメッセージに返信します", label));
  }
}
