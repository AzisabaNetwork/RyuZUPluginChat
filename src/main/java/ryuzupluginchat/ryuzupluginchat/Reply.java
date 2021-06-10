package ryuzupluginchat.ryuzupluginchat;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Reply implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (command.getName().equalsIgnoreCase("reply") || command.getName().equalsIgnoreCase("r")) {
            if(sender instanceof Player) {
                if (args.length <= 0) {
                    sender.sendMessage(ChatColor.RED + "/" + label + " [Message]");
                    return true;
                }
                Player p = (Player) sender;
                if (!RyuZUPluginChat.reply.containsKey(p.getName())) {
                    sender.sendMessage(ChatColor.RED + "過去にプライベートメッセージをやり取りしたプレイヤーがいません");
                    return true;
                }
                String msg = "";
                for(int i = 0 ; i < args.length ; i++) {
                    if(i == 0) {
                        msg += args[i];
                    } else {
                        msg += (" " + args[i]);
                    }
                }
                RyuZUPluginChat.ryuzupluginchat.sendPrivateMessage(p , args[0] , RyuZUPluginChat.reply.get(p.getName()));
            }
        }
        return true;
    }
}
