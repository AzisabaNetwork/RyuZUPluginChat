package ryuzupluginchat.ryuzupluginchat;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Command implements CommandExecutor,TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (command.getName().equalsIgnoreCase("rpc")) {
            if (args.length <= 0) {
                sender.sendMessage(ChatColor.GOLD + "------------------------使い方------------------------");
                if(sender.hasPermission("rpc.op")) {
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
                if(sender instanceof Player) {
                    if (args.length <= 2) {
                        sender.sendMessage(ChatColor.RED + "/" + label + " tell [MCID] [Message]");
                        return true;
                    }
                    Player p = (Player) sender;
                    if (args[1].equals(p.getName())) {
                        sender.sendMessage(ChatColor.RED + "自分にプライベートメッセージを送ることはできません");
                        return true;
                    }
                    RyuZUPluginChat.ryuzupluginchat.sendPrivateMessage(p , args[2] , args[1]);
                }
                return true;
            }

            if (args[0].equalsIgnoreCase("reply")) {
                if(sender instanceof Player) {
                    if (args.length <= 1) {
                        sender.sendMessage(ChatColor.RED + "/" + label + " reply [Message]");
                        return true;
                    }
                    Player p = (Player) sender;
                    if (!RyuZUPluginChat.reply.containsKey(p.getName())) {
                        sender.sendMessage(ChatColor.RED + "過去にプライベートメッセージをやり取りしたプレイヤーがいません");
                        return true;
                    }
                    RyuZUPluginChat.ryuzupluginchat.sendPrivateMessage(p , args[2] , RyuZUPluginChat.reply.get(p.getName()));
                }
                return true;
            }

            if (args[0].equalsIgnoreCase("prefix") || args[0].equalsIgnoreCase("p")) {
                if (!sender.hasPermission("rpc.op")) {
                    sender.sendMessage(ChatColor.RED + "ぽまえけんげんないやろ");
                    return true;
                }
                if (args.length <= 1) {
                    sender.sendMessage(ChatColor.RED + "/" + label + " prefix [set] [MCID] [Prefix]");
                    return true;
                }
                if(args[1].equalsIgnoreCase("set")) {
                    if (args.length <= 3) {
                        sender.sendMessage(ChatColor.RED + "/" + label + " prefix set [MCID] [Prefix]");
                        return true;
                    }
                    RyuZUPluginChat.ryuzupluginchat.setPrefix(args[2] , args[3]);
                }
                return true;
            }

            if (args[0].equalsIgnoreCase("suffix") || args[0].equalsIgnoreCase("s")) {
                if (!sender.hasPermission("rpc.op")) {
                    sender.sendMessage(ChatColor.RED + "ぽまえけんげんないやろ");
                    return true;
                }
                if (args.length <= 1) {
                    sender.sendMessage(ChatColor.RED + "/" + label + " suffix [set] [MCID] [Suffit]");
                    return true;
                }
                if(args[1].equalsIgnoreCase("set")) {
                    if (args.length <= 3) {
                        sender.sendMessage(ChatColor.RED + "/" + label + " suffix set [MCID] [Suffit]");
                        return true;
                    }
                    RyuZUPluginChat.ryuzupluginchat.setSuffix(args[2] , args[3]);
                }
                return true;
            }

            if (args[0].equalsIgnoreCase("config") || args[0].equalsIgnoreCase("c")) {
                if (!sender.hasPermission("rpc.op")) {
                    sender.sendMessage(ChatColor.RED + "ぽまえけんげんないやろ");
                    return true;
                }
                if (args.length <= 1) {
                    sender.sendMessage(ChatColor.BLUE + "/" + label + " config [format/list/group]:コンフィグを編集します");
                    return true;
                }
                if (args[1].equalsIgnoreCase("format")) {
                    if (args.length <= 4) {
                        sender.sendMessage(ChatColor.BLUE + "/" + label + " config format [set] [GroupName] [format]:formatを編集します");
                        return true;
                    }
                    if(args[2].equalsIgnoreCase("set")) {
                        String format = "";
                        for(int i = 0 ; i < args.length ; i++) {
                            if(i == 4) {
                                format += args[i];
                            } else if(i > 4) {
                                format += (" " + args[i]);
                            }
                        }
                        RyuZUPluginChat.ryuzupluginchat.setFormat(args[3] , format);
                        sender.sendMessage(ChatColor.GREEN + "Formatを編集しました");
                        return true;
                    }
                }
                if (args[1].equalsIgnoreCase("list")) {
                    if (args.length <= 4) {
                        sender.sendMessage(ChatColor.BLUE + "/" + label + " config list [add/remove] [GroupName] [ServerName]:共有するServerListを編集します");
                        return true;
                    }
                    if(args[2].equalsIgnoreCase("add")) {
                        RyuZUPluginChat.ryuzupluginchat.addServer(args[3] , args[4]);
                        sender.sendMessage(ChatColor.GREEN + "listに追加しました");
                        return true;
                    }
                    if(args[2].equalsIgnoreCase("remove")) {
                        RyuZUPluginChat.ryuzupluginchat.removeServer(args[3] , args[4]);
                        sender.sendMessage(ChatColor.GREEN + "listから削除しました");
                        return true;
                    }
                }
                if (args[1].equalsIgnoreCase("group")) {
                    if (args.length <= 3) {
                        sender.sendMessage(ChatColor.BLUE + "/" + label + " config group [remove] [GroupName]:共有するGroupを編集します");
                        return true;
                    }
                    if(args[2].equalsIgnoreCase("remove")) {
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
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> list = new ArrayList<>();
        if(sender instanceof Player) {
            Player p = (Player) sender;
            if (command.getName().equalsIgnoreCase("rpc")) {
                if(args.length == 1) {
                    if(sender.hasPermission("rpc.op")) {list.addAll(Arrays.asList("prefix" , "suffix" , "config"));}
                    list.addAll(Arrays.asList("tell" , "reply"));
                }
                if(args.length == 2) {
                    if(sender.hasPermission("rpc.op")) {
                        if(args[0].equals("prefix") || args[0].equals("suffix")) {
                            list.add("set");
                        }
                        if(args[0].equals("config")) {
                            list.addAll(Arrays.asList("format" , "list" , "group"));
                        }
                    }
                    if(args[0].equals("tell")) {
                        list.addAll(RyuZUPluginChat.getPlayers().stream().filter(l -> !l.equals(p.getName())).collect(Collectors.toList()));
                    }
                }
                if(args.length == 3) {
                    if(sender.hasPermission("rpc.op")) {
                        if(args[1].equals("format")) {
                            list.add("set");
                        }
                        if(args[1].equals("list")) {
                            list.addAll(Arrays.asList("add" , "remove"));
                        }
                        if(args[1].equals("group")) {
                            list.add("remove");
                        }
                    }
                }
            }
        }
        return list;
    }
}
