package net.azisaba.ryuzupluginchat.command;

import lombok.RequiredArgsConstructor;
import net.azisaba.ryuzupluginchat.RyuZUPluginChat;
import net.azisaba.ryuzupluginchat.util.Chat;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class ReplyCommand implements CommandExecutor {

  private final RyuZUPluginChat plugin;

  @Override
  public boolean onCommand(
      @NotNull CommandSender sender,
      @NotNull Command command,
      @NotNull String label,
      @NotNull String[] args) {
    if (!(sender instanceof Player)) {
      return true;
    }

    if (args.length <= 0) {
      sender.sendMessage(Chat.f("&c使い方: /{0} <メッセージ>", label));
      return true;
    }
    Player p = (Player) sender;

    RyuZUPluginChat.newChain()
        .asyncFirst(() -> plugin.getReplyTargetFetcher().getReplyTarget(p))
        .async(
            (uuid) -> {
              if (uuid == null) {
                sender.sendMessage(Chat.f("過去にプライベートメッセージをやり取りしたプレイヤーがいません"));
                return null;
              }

              if (!plugin.getPlayerUUIDMapContainer().isOnline(uuid)) {
                sender.sendMessage(Chat.f("&c過去にプライベートメッセージをやり取りしたプレイヤーはオフラインです"));
                return null;
              }

              String msg = String.join(" ", args);
              return plugin.getMessageDataFactory().createPrivateMessageData(p, uuid, msg);
            })
        .abortIfNull()
        .sync(
            (data) -> {
              plugin.getPrivateChatResponseWaiter().register(data.getId(), data, 5000L);
              return data;
            })
        .asyncLast((data) -> plugin.getPublisher().publishPrivateMessage(data))
        .execute();
    return true;
  }
}
