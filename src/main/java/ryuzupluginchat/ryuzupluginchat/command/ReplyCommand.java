package ryuzupluginchat.ryuzupluginchat.command;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ryuzupluginchat.ryuzupluginchat.RyuZUPluginChat;
import ryuzupluginchat.ryuzupluginchat.util.message.PrivateMessageData;

@RequiredArgsConstructor
public class ReplyCommand implements CommandExecutor {

  private final RyuZUPluginChat plugin;

  @Override
  public boolean onCommand(@NotNull CommandSender sender,
      org.bukkit.command.@NotNull Command command, @NotNull String label, @NotNull String[] args) {
    if (!(sender instanceof Player)) {
      return true;
    }
    if (args.length <= 0) {
      sender.sendMessage(ChatColor.RED + "/" + label + " [Message]");
      return true;
    }
    Player p = (Player) sender;
    String targetName = plugin.getReplyTargetContainer().getReplyPlayer(p);

    if (targetName == null) {
      sender.sendMessage(ChatColor.RED + "過去にプライベートメッセージをやり取りしたプレイヤーがいません");
      return true;
    }

    UUID targetUUID = plugin.getTabCompletePlayerNameContainer().getUUID(targetName);
    if (targetUUID == null) {
      sender.sendMessage(ChatColor.RED + "過去にプライベートメッセージをやり取りしたプレイヤーはオフラインです");
      return true;
    }

    String msg = String.join(" ", args).substring(("/" + label + " " + args[0] + " ").length());

    PrivateMessageData data = plugin.getMessageDataFactory()
        .createPrivateMessageData(p, targetUUID, msg);
    RyuZUPluginChat.newChain()
        .async(() -> plugin.getPublisher().publishPrivateMessage(data)).execute();
    return true;
  }
}
