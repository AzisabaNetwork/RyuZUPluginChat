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

public class Command implements CommandExecutor,TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (command.getName().equalsIgnoreCase("rpc")) {
            if (args.length <= 0) {
                sender.sendMessage(ChatColor.GOLD + "------------------------使い方------------------------");
                if(!sender.hasPermission("rpc.op")) {
                    sender.sendMessage(ChatColor.BLUE + "/" + label + " prefix :Prefixを編集します");
                    sender.sendMessage(ChatColor.BLUE + "/" + label + " suffix :Suffixを編集します");
                    sender.sendMessage(ChatColor.BLUE + "/" + label + " config :Configを編集します");
                }
                sender.sendMessage(ChatColor.BLUE + "/" + label + " tell :プレイヤーにプライベートメッセージを送信します");
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
                    RyuZUPluginChat.ryuzupluginchat.sendPrivateMessage(p , args[2] , args[1]);
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
                if (args.length <= 1) {
                    sender.sendMessage(ChatColor.BLUE + "/" + label + " config [fromat/list]:コンフィグを編集します");
                    return true;
                }
                if (args[1].equalsIgnoreCase("fromat")) {
                    if (args.length <= 4) {
                        sender.sendMessage(ChatColor.BLUE + "/" + label + " config fromat [set] [GroupName] [Fromat]:fromatを編集します");
                        return true;
                    }
                    if(args[2].equalsIgnoreCase("set")) {
                        RyuZUPluginChat.ryuzupluginchat.setFormat(args[3] , args[4]);
                        sender.sendMessage(ChatColor.GREEN + "fromatを編集しました");
                        return true;
                    }
                }
                if (args[1].equalsIgnoreCase("list")) {
                    if (args.length <= 4) {
                        sender.sendMessage(ChatColor.BLUE + "/" + label + " config fromat [add/remove] [GroupName] [ServerName]:共有するServerListを編集します");
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
            }
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> list = new ArrayList<>();
        if (command.getName().equalsIgnoreCase("rpc")) {
            if(args.length == 1) {
                if(sender.hasPermission("rpc.op")) {list.addAll(Arrays.asList("prefix" , "suffix" , "config"));}
                list.add("tell");
            }
            if(args.length == 2) {
                if(sender.hasPermission("rpc.op")) {
                    if(args[0].equals("prefix") || args[0].equals("suffix")) {
                        list.add("set");
                    }
                    if(args[0].equals("config")) {
                        list.addAll(Arrays.asList("format" , "list"));
                    }
                }
            }
            if(args.length == 3) {
                if(sender.hasPermission("rpc.op")) {
                    if(args[0].equals("format")) {
                        list.add("set");
                    }
                    if(args[0].equals("list")) {
                        list.addAll(Arrays.asList("add" , "remove"));
                    }
                }
            }
        }
        return list;
    }
}
