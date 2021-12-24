package ryuzupluginchat.ryuzupluginchat.command;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ryuzupluginchat.ryuzupluginchat.RyuZUPluginChat;
import ryuzupluginchat.ryuzupluginchat.message.data.PrivateMessageData;

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
    UUID targetUUID = plugin.getReplyTargetFetcher().getReplyTarget(p);

    if (targetUUID == null) {
      sender.sendMessage(ChatColor.RED + "過去にプライベートメッセージをやり取りしたプレイヤーがいません");
      return true;
    }

    if (!plugin.getPlayerUUIDMapContainer().isOnline(targetUUID)) {
      sender.sendMessage(ChatColor.RED + "過去にプライベートメッセージをやり取りしたプレイヤーはオフラインです");
      return true;
    }

    String msg = String.join(" ", args);

    PrivateMessageData data = plugin.getMessageDataFactory()
        .createPrivateMessageData(p, targetUUID, msg);

    RyuZUPluginChat.newChain()
        .sync(() -> plugin.getPrivateChatResponseWaiter().register(data.getId(), data, 5000L))
        .async(() -> plugin.getPublisher().publishPrivateMessage(data)).execute();
    return true;
  }
}
