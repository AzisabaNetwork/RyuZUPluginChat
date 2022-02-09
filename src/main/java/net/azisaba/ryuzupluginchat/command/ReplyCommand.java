package net.azisaba.ryuzupluginchat.command;

import lombok.RequiredArgsConstructor;
import net.azisaba.ryuzupluginchat.RyuZUPluginChat;
import net.md_5.bungee.api.ChatColor;
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
      org.bukkit.command.@NotNull Command command,
      @NotNull String label,
      @NotNull String[] args) {
    if (!(sender instanceof Player)) {
      return true;
    }
    if (args.length <= 0) {
      sender.sendMessage(ChatColor.RED + "/" + label + " [Message]");
      return true;
    }
    Player p = (Player) sender;

    RyuZUPluginChat.newChain()
        .asyncFirst(() -> plugin.getReplyTargetFetcher().getReplyTarget(p))
        .async(
            (uuid) -> {
              if (uuid == null) {
                sender.sendMessage(ChatColor.RED + "過去にプライベートメッセージをやり取りしたプレイヤーがいません");
                return null;
              }

              if (!plugin.getPlayerUUIDMapContainer().isOnline(uuid)) {
                sender.sendMessage(ChatColor.RED + "過去にプライベートメッセージをやり取りしたプレイヤーはオフラインです");
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
