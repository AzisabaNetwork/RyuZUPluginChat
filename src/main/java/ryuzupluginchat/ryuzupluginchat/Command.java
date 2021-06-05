package ryuzupluginchat.ryuzupluginchat;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class Command implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (command.getName().equalsIgnoreCase("rms") || command.getName().equalsIgnoreCase("ryuzumagicskills")) {
            if (!sender.hasPermission("rms.op")) {
                sender.sendMessage(ChatColor.RED + "ぽまえけんげんないやろ");
                return true;
            }
            if (args.length <= 0) {
                sender.sendMessage(ChatColor.GOLD + "------------------------使い方------------------------");
                //sender.sendMessage(ChatColor.BLUE + "/" + label + " reload :リロード");
                sender.sendMessage(ChatColor.BLUE + "/" + label + " prefix :Itemを編集します");
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
                if(args[1].equals("set")) {
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
                if(args[1].equals("set")) {
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
}
