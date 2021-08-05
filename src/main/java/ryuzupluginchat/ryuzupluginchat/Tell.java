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

public class Tell implements CommandExecutor,TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (command.getName().equalsIgnoreCase("tell") || command.getName().equalsIgnoreCase("t")) {
                if(sender instanceof Player) {
                    if (args.length <= 1) {
                        sender.sendMessage(ChatColor.RED + "/" + label + " [MCID] [Message]");
                        return true;
                    }
                    Player p = (Player) sender;
                    if (args[0].equals(p.getName())) {
                        sender.sendMessage(ChatColor.RED + "自分にプライベートメッセージを送ることはできません");
                        return true;
                    }
                    if (RyuZUPluginChat.isMuted(p)) {
                        sender.sendMessage(ChatColor.RED + "あなたはミュートされています");
                        return true;
                    }
                    String msg = "";
                    for(int i = 1 ; i < args.length ; i++) {
                        if(i == 1) {
                            msg += args[i];
                        } else {
                            msg += (" " + args[i]);
                        }
                    }
                    RyuZUPluginChat.ryuzupluginchat.sendPrivateMessage(p , msg , args[0]);
                }
            }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> list = new ArrayList<>();
        if(sender instanceof Player) {
            Player p = (Player) sender;
            if (command.getName().equalsIgnoreCase("tell") || command.getName().equalsIgnoreCase("t")) {
                if(args.length == 1) {
                    list.addAll(RyuZUPluginChat.getPlayers().stream().filter(l -> !l.equals(p.getName())).collect(Collectors.toList()));
                }
            }
        }
        return list;
    }
}
