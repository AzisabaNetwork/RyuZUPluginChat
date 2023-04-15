package net.azisaba.ryuzupluginchat.command;

import lombok.RequiredArgsConstructor;
import net.azisaba.ryuzupluginchat.RyuZUPluginChat;
import net.azisaba.ryuzupluginchat.localization.Messages;
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
      Messages.sendFormatted(sender, "command.error.sender_not_player");
      return true;
    }

    if (args.length == 0) {
      Messages.sendFormatted(sender, "command.reply.usage", label);
      return true;
    }
    Player p = (Player) sender;

    RyuZUPluginChat.newChain()
        .asyncFirst(() -> plugin.getReplyTargetFetcher().getReplyTarget(p))
        .async(
            (uuid) -> {
              if (uuid == null) {
                Messages.sendFormatted(sender, "command.reply.error.no_recent_messages");
                return null;
              }

              if (!plugin.getPlayerUUIDMapContainer().isOnline(uuid) || plugin.getVanishController().isVanished(uuid)) {
                Messages.sendFormatted(sender, "command.reply.error.target_offline");
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
