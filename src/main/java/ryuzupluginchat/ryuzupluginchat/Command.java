package ryuzupluginchat.ryuzupluginchat;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Command implements CommandExecutor,TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (command.getName().equalsIgnoreCase("rpc")) {
            if (!sender.hasPermission("rpc.op")) {
                sender.sendMessage(ChatColor.RED + "ぽまえけんげんないやろ");
                return true;
            }
            if (args.length <= 0) {
                sender.sendMessage(ChatColor.GOLD + "------------------------使い方------------------------");
                if(!sender.hasPermission("rpc.op")) {
                    sender.sendMessage(ChatColor.BLUE + "/" + label + " prefix :Prefixを編集します");
                    sender.sendMessage(ChatColor.BLUE + "/" + label + " suffix :Prefixを編集します");
                }
                sender.sendMessage(ChatColor.BLUE + "/" + label + " tell :プレイヤーにプライベートメッセージを送信します");
                return true;
            }

            /*if (args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("r")) {
                sender.sendMessage(ChatColor.GREEN + "リロード完了");
                return true;
            }*/

            if (args[0].equalsIgnoreCase("prefix") || args[0].equalsIgnoreCase("p")) {
                if (args.length <= 1) {
                    sender.sendMessage(ChatColor.RED + "/" + label + " prefix [set] [MCID] + [Prefix]");
                    return true;
                }
                if(args[1].equalsIgnoreCase("set")) {
                    if (args.length <= 3) {
                        sender.sendMessage(ChatColor.RED + "/" + label + " prefix set [MCID] + [Prefix]");
                        return true;
                    }
                    RyuZUPluginChat.ryuzupluginchat.setPrefix(args[2] , args[3]);
                }
                return true;
            }

            if (args[0].equalsIgnoreCase("suffix") || args[0].equalsIgnoreCase("s")) {
                if (args.length <= 1) {
                    sender.sendMessage(ChatColor.RED + "/" + label + " suffix [set] [MCID] + [Suffit]");
                    return true;
                }
                if(args[1].equalsIgnoreCase("set")) {
                    if (args.length <= 3) {
                        sender.sendMessage(ChatColor.RED + "/" + label + " suffix set [MCID] + [Suffit]");
                        return true;
                    }
                    RyuZUPluginChat.ryuzupluginchat.setSuffix(args[2] , args[3]);
                }
                return true;
            }
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> list = new ArrayList<>();
        if (command.getName().equalsIgnoreCase("rpc")) {
            if(args.length == 1) {
                if(sender.hasPermission("rpc.op")) {list.addAll(Arrays.asList("prefix" , "suffix"));}
                list.add("tell");
            }
            if(args.length == 2) {
                if(sender.hasPermission("rpc.op")) {
                    if(args[0].equals("prefix") || args[0].equals("suffix")) {
                        list.add("set");
                    }
                }
            }
        }
        return list;
    }
}
