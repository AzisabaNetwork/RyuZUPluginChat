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
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ryuzupluginchat.ryuzupluginchat.RyuZUPluginChat;
import ryuzupluginchat.ryuzupluginchat.util.message.SystemMessageData;

@RequiredArgsConstructor
public class RPCCommand implements CommandExecutor, TabCompleter {

  private final RyuZUPluginChat plugin;

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

            /*if (args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("r")) {
                sender.sendMessage(ChatColor.GREEN + "リロード完了");
                return true;
            }*/

      if (args[0].equalsIgnoreCase("tell")) {
        String tellCommand = String.join(" ", args);
        Bukkit.dispatchCommand(sender, "/" + plugin.getName().toLowerCase() + ":" + tellCommand);
        return true;
      }

      if (args[0].equalsIgnoreCase("reply")) {
        String replyCommand = String.join(" ", args);
        Bukkit.dispatchCommand(sender, "/" + plugin.getName().toLowerCase() + ":" + replyCommand);
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
          UUID uuid = plugin.getTabCompletePlayerNameContainer().getUUID(args[1]);
          if (uuid == null) {
            sender.sendMessage(ChatColor.RED + "プレイヤーが見つかりませんでした");
            return true;
          }
          String prefix = String.join(" ", args).substring((args[0] + args[1]).length() + 2);
          SystemMessageData data = plugin.getMessageDataFactory()
              .createPrefixSystemChatMessageData(uuid, prefix);

          RyuZUPluginChat.newChain().async(() -> plugin.getPublisher().publishSystemMessage(data))
              .execute();
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
          UUID uuid = plugin.getTabCompletePlayerNameContainer().getUUID(args[1]);
          if (uuid == null) {
            sender.sendMessage(ChatColor.RED + "プレイヤーが見つかりませんでした");
            return true;
          }
          String suffix = String.join(" ", args).substring((args[0] + args[1]).length() + 2);
          SystemMessageData data = plugin.getMessageDataFactory()
              .createSuffixSystemChatMessageData(uuid, suffix);

          RyuZUPluginChat.newChain().async(() -> plugin.getPublisher().publishSystemMessage(data))
              .execute();
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
        String msg = "";
        for (int i = (args[1].equalsIgnoreCase("message") ? 2 : 3); i < args.length; i++) {
          if (i == (args[1].equalsIgnoreCase("message") ? 2 : 3)) {
            msg += args[i];
          } else {
            msg += (" " + args[i]);
          }
        }
        if (args[1].equalsIgnoreCase("message")) {
          RyuZUPluginChat.ryuzupluginchat.sendSystemMessage(msg);
        } else if (args[1].equalsIgnoreCase("player")) {
          Player p = Bukkit.getPlayer(args[2]);
          if (p == null) {
            return true;
          }
          RyuZUPluginChat.ryuzupluginchat.sendSystemMessage(msg, p);
        } else if (args[1].equalsIgnoreCase("playermessage")) {
          Player p = Bukkit.getPlayer(args[2]);
          if (p == null) {
            return true;
          }
          boolean global = RyuZUPluginChat.lunachatapi.getDefaultChannel(p.getName()) == null;
          if (global || msg.substring(0, 1).equals("!")) {
            RyuZUPluginChat.sendGlobalMessage(p,
                msg.substring(0, 1).equals("!") ? msg.substring(1) : msg);
          } else {
            RyuZUPluginChat.sendChannelMessage(p, msg,
                RyuZUPluginChat.lunachatapi.getDefaultChannel(p.getName()));
          }
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
              + " config [format/channelformat/tellformat/list/group]:コンフィグを編集します");
          return true;
        }
        if (args[1].equalsIgnoreCase("format")) {
          if (args.length <= 4) {
            sender.sendMessage(ChatColor.BLUE + "/" + label
                + " config format [set] [GroupName] [format]:formatを編集します");
            return true;
          }
          if (args[2].equalsIgnoreCase("set")) {
            String format = "";
            for (int i = 4; i < args.length; i++) {
              if (i == 4) {
                format += args[i];
              } else {
                format += (" " + args[i]);
              }
            }
            RyuZUPluginChat.ryuzupluginchat.setFormat(args[3], format);
            sender.sendMessage(ChatColor.GREEN + "Formatを編集しました");
            return true;
          }
        }
        if (args[1].equalsIgnoreCase("channelformat")) {
          if (args.length <= 4) {
            sender.sendMessage(ChatColor.BLUE + "/" + label
                + " config channelformat [set] [GroupName] [format]:ChannelFormatを編集します");
            return true;
          }
          if (args[2].equalsIgnoreCase("set")) {
            String format = "";
            for (int i = 4; i < args.length; i++) {
              if (i == 4) {
                format += args[i];
              } else {
                format += (" " + args[i]);
              }
            }
            RyuZUPluginChat.ryuzupluginchat.setChannelFormat(args[3], format);
            sender.sendMessage(ChatColor.GREEN + "ChannelFormatを編集しました");
            return true;
          }
        }
        if (args[1].equalsIgnoreCase("tellformat")) {
          if (args.length <= 4) {
            sender.sendMessage(ChatColor.BLUE + "/" + label
                + " config tellformat [set] [GroupName] [format]:TellFormatを編集します");
            return true;
          }
          if (args[2].equalsIgnoreCase("set")) {
            String format = "";
            for (int i = 4; i < args.length; i++) {
              if (i == 4) {
                format += args[i];
              } else {
                format += (" " + args[i]);
              }
            }
            RyuZUPluginChat.ryuzupluginchat.setTellFormat(args[3], format);
            sender.sendMessage(ChatColor.GREEN + "TellFormatを編集しました");
            return true;
          }
        }
        if (args[1].equalsIgnoreCase("list")) {
          if (args.length <= 4) {
            sender.sendMessage(ChatColor.BLUE + "/" + label
                + " config list [add/remove] [GroupName] [ServerName]:共有するServerListを編集します");
            return true;
          }
          if (args[2].equalsIgnoreCase("add")) {
            RyuZUPluginChat.ryuzupluginchat.addServer(args[3], args[4]);
            sender.sendMessage(ChatColor.GREEN + "listに追加しました");
            return true;
          }
          if (args[2].equalsIgnoreCase("remove")) {
            RyuZUPluginChat.ryuzupluginchat.removeServer(args[3], args[4]);
            sender.sendMessage(ChatColor.GREEN + "listから削除しました");
            return true;
          }
        }
        if (args[1].equalsIgnoreCase("group")) {
          if (args.length <= 3) {
            sender.sendMessage(ChatColor.BLUE + "/" + label
                + " config group [remove] [GroupName]:共有するGroupを編集します");
            return true;
          }
          if (args[2].equalsIgnoreCase("remove")) {
            RyuZUPluginChat.ryuzupluginchat.removeGroup(args[3]);
            sender.sendMessage(ChatColor.GREEN + "Groupを削除しました");
            return true;
          }
        }
      }
    }
    return true;
  }

  @Override
  public @Nullable List<String> onTabComplete(@NotNull CommandSender sender,
      org.bukkit.command.@NotNull Command command, @NotNull String alias, @NotNull String[] args) {
    List<String> list = new ArrayList<>();
    if (sender instanceof Player) {
      Player p = (Player) sender;
      if (command.getName().equalsIgnoreCase("rpc")) {
        if (args.length == 1) {
          if (sender.hasPermission("rpc.op")) {
            list.addAll(Arrays.asList("prefix", "suffix", "message", "config"));
          }
          list.addAll(Arrays.asList("tell", "reply"));
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
          if (args[0].equals("tell")) {
            list.addAll(RyuZUPluginChat.getPlayers().stream().filter(l -> !l.equals(p.getName()))
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
            if (Arrays.asList("message", "player", "playermessage").contains(args[1])) {
              list.addAll(Bukkit.getOnlinePlayers().stream().map(HumanEntity::getName)
                  .collect(Collectors.toList()));
            }
          }
        }
      }
    }
    return list;
  }
}
