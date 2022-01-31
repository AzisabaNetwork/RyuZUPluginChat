package ryuzupluginchat.ryuzupluginchat.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ryuzupluginchat.ryuzupluginchat.RyuZUPluginChat;
import ryuzupluginchat.ryuzupluginchat.message.data.SystemMessageData;

@RequiredArgsConstructor
public class RPCCommand implements CommandExecutor, TabCompleter {

  private final RyuZUPluginChat plugin;

  private final List<String> redirectArgs = Arrays.asList("tell", "reply", "hide", "unhide");

  @Override
  public boolean onCommand(@NotNull CommandSender sender,
      org.bukkit.command.@NotNull Command command, @NotNull String label, @NotNull String[] args) {
    if (command.getName().equalsIgnoreCase("rpc")) {
      if (args.length <= 0) {
        sender.sendMessage(ChatColor.GOLD + "------------------------使い方------------------------");
        if (sender.hasPermission("rpc.op")) {
          sender.sendMessage(ChatColor.BLUE + "/" + label + " prefix :Prefixを編集します");
          sender.sendMessage(ChatColor.BLUE + "/" + label + " suffix :Suffixを編集します");
          sender.sendMessage(ChatColor.BLUE + "/" + label + " config :Configを編集します");
        }
        sender.sendMessage(ChatColor.BLUE + "/" + label + " tell :プレイヤーにプライベートメッセージを送信します");
        sender.sendMessage(ChatColor.BLUE + "/" + label + " reply :プレイべートメッセージを送り返します");
        return true;
      }

      if (args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("r")) {
        if (!sender.hasPermission("rpc.op")) {
          sender.sendMessage(ChatColor.RED + "ぽまえけんげんないやろ");
          return true;
        }
        
        sender.sendMessage(ChatColor.YELLOW + "非同期でリロードを実行しています...");
        RyuZUPluginChat.newSharedChain("reload")
            .async(() -> {
              long start = System.currentTimeMillis();

              plugin.getRpcConfig().reloadConfig();
              plugin.executeFullReload();

              long end = System.currentTimeMillis();
              sender.sendMessage(ChatColor.GREEN
                  + "非同期でシステムをリロードしました " + ChatColor.GRAY + "(" + (end - start) + "ms)");
            }).execute();
        return true;
      }

      if (redirectArgs.contains(args[0])) {
        String tellCommand = String.join(" ", args);
        Bukkit.dispatchCommand(sender, plugin.getName().toLowerCase() + ":" + tellCommand);
        return true;
      }

      if (args[0].equalsIgnoreCase("prefix") || args[0].equalsIgnoreCase("p")) {
        if (!sender.hasPermission("rpc.op")) {
          sender.sendMessage(ChatColor.RED + "ぽまえけんげんないやろ");
          return true;
        }
        if (args.length <= 1) {
          sender.sendMessage(ChatColor.RED + "/" + label + " prefix set [MCID] [Prefix]");
          return true;
        }
        if (args[1].equalsIgnoreCase("set")) {
          if (args.length <= 3) {
            sender.sendMessage(ChatColor.RED + "/" + label + " prefix set [MCID] [Prefix]");
            return true;
          }
          UUID uuid = plugin.getPlayerUUIDMapContainer().getUUID(args[2]);
          if (uuid == null) {
            sender.sendMessage(ChatColor.RED + "プレイヤーが見つかりませんでした");
            return true;
          }
          String prefix = String.join(" ", args)
              .substring((args[0] + args[1] + args[2]).length() + 3);
          plugin.getPrefixSuffixContainer().setPrefix(uuid, prefix, true);
          sender.sendMessage(
              ChatColor.YELLOW + args[2] + ChatColor.GREEN + "のPrefixを " + ChatColor.RESET
                  + ChatColor.translateAlternateColorCodes('&', prefix) + ChatColor.GREEN
                  + " に変更しました");
        }
        return true;
      }

      if (args[0].equalsIgnoreCase("suffix") || args[0].equalsIgnoreCase("s")) {
        if (!sender.hasPermission("rpc.op")) {
          sender.sendMessage(ChatColor.RED + "ぽまえけんげんないやろ");
          return true;
        }
        if (args.length <= 1) {
          sender.sendMessage(ChatColor.RED + "/" + label + " suffix [set] [MCID] [Suffix]");
          return true;
        }
        if (args[1].equalsIgnoreCase("set")) {
          if (args.length <= 3) {
            sender.sendMessage(ChatColor.RED + "/" + label + " suffix set [MCID] [Suffix]");
            return true;
          }
          UUID uuid = plugin.getPlayerUUIDMapContainer().getUUID(args[2]);
          if (uuid == null) {
            sender.sendMessage(ChatColor.RED + "プレイヤーが見つかりませんでした");
            return true;
          }
          String suffix = String.join(" ", args)
              .substring((args[0] + args[1] + args[2]).length() + 3);

          plugin.getPrefixSuffixContainer().setSuffix(uuid, suffix, true);
          sender.sendMessage(
              ChatColor.YELLOW + args[2] + ChatColor.GREEN + "のSuffixを " + ChatColor.RESET
                  + ChatColor.translateAlternateColorCodes('&', suffix) + ChatColor.GREEN
                  + " に変更しました");
        }
        return true;
      }

      if (args[0].equalsIgnoreCase("message") || args[0].equalsIgnoreCase("msg")) {
        if (!sender.hasPermission("rpc.op")) {
          sender.sendMessage(ChatColor.RED + "ぽまえけんげんないやろ");
          return true;
        }

        if (args.length <= 2) {
          sender.sendMessage(ChatColor.BLUE + "/" + label
              + " message [message/player/playermessage] [Message]:指定されたメッセージをGroupに送信します");
          return true;
        }

        String msg = String.join(" ", args).substring(args[0].length() + args[1].length() + 2);
        if (args[1].equalsIgnoreCase("message")) {
          SystemMessageData data = plugin.getMessageDataFactory()
              .createGeneralSystemChatMessageData(msg);
          plugin.getPublisher().publishSystemMessage(data);
        } else if (args[1].equalsIgnoreCase("player")) {
          msg = msg.substring(args[2].length() + 1);
          UUID uuid = plugin.getPlayerUUIDMapContainer().getUUID(args[2]);
          if (uuid == null) {
            sender.sendMessage(
                ChatColor.YELLOW + args[2] + ChatColor.RED + "という名前のプレイヤーが見つかりませんでした");
            return true;
          }

          SystemMessageData data = plugin.getMessageDataFactory()
              .createPrivateSystemChatMessageData(uuid, msg);
          RyuZUPluginChat.newChain()
              .async(() -> plugin.getPublisher().publishSystemMessage(data))
              .execute();
        } else if (args[1].equalsIgnoreCase("playermessage")) {
          msg = msg.substring(args[2].length() + 1);
          UUID uuid = plugin.getPlayerUUIDMapContainer().getUUID(args[2]);
          if (uuid == null) {
            sender.sendMessage(
                ChatColor.YELLOW + args[2] + ChatColor.RED + "という名前のプレイヤーが見つかりませんでした");
            return true;
          }

          SystemMessageData data = plugin.getMessageDataFactory()
              .createImitationChatMessageData(uuid, msg);

          RyuZUPluginChat.newChain()
              .async(() -> plugin.getPublisher().publishSystemMessage(data))
              .execute();
        }
        return true;
      }

      if (args[0].equalsIgnoreCase("config") || args[0].equalsIgnoreCase("c")) {
        if (!sender.hasPermission("rpc.op")) {
          sender.sendMessage(ChatColor.RED + "ぽまえけんげんないやろ");
          return true;
        }
        if (args.length <= 1) {
          sender.sendMessage(ChatColor.BLUE + "/" + label
              + " config format set <global/private> [format]: formatを編集します");
          return true;
        }

        if (args[1].equalsIgnoreCase("format")) {
          if (args.length <= 4) {
            sender.sendMessage(ChatColor.BLUE + "/" + label
                + " config format set <global/private> [format]: formatを編集します");
            return true;
          }
          if (args[2].equalsIgnoreCase("set")) {
            String format = String.join(" ", args)
                .substring((args[0] + args[1] + args[2] + args[3]).length() + 4);

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
                sender.sendMessage(ChatColor.BLUE + "/" + label
                    + " config format set <global/private/channel> [format]: formatを編集します");
                return true;
            }
            sender.sendMessage(ChatColor.GREEN + "Formatを編集しました");
            return true;
          }
        } else {
          sender.sendMessage(ChatColor.BLUE + "/" + label
              + " config format set <global/private/channel> [format]: formatを編集します");
          return true;
        }
      }
    }
    return true;
  }

  @Override
  public @Nullable List<String> onTabComplete(@NotNull CommandSender sender,
      org.bukkit.command.@NotNull Command command, @NotNull String alias, @NotNull String[] args) {
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
        list.addAll(plugin.getPlayerUUIDMapContainer().getAllNames().stream()
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
}
