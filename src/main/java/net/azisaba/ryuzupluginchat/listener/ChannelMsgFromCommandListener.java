package net.azisaba.ryuzupluginchat.listener;

import com.github.ucchyocean.lc3.LunaChat;
import com.github.ucchyocean.lc3.channel.Channel;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import net.azisaba.ryuzupluginchat.RyuZUPluginChat;
import net.azisaba.ryuzupluginchat.message.data.ChannelChatMessageData;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

@RequiredArgsConstructor
public class ChannelMsgFromCommandListener implements Listener {

  private final RyuZUPluginChat plugin;

  private final List<String> chCommandList =
      Arrays.asList(
          "/ch", "/lunachat:ch", "/lc", "/lunachat:lc", "/lunachat", "/lunachat:lunachat");

  private final List<String> lunaChatSubCommands =
      Arrays.asList(
          "create",
          "join",
          "leave",
          "list",
          "info",
          "log",
          "accept",
          "deny",
          "hide",
          "unhide",
          "invite",
          "mute",
          "unmute",
          "kick",
          "ban",
          "pardon",
          "remove",
          "moderator",
          "option");

  @EventHandler
  public void onCommand(PlayerCommandPreprocessEvent e) {
    String message = e.getMessage();
    String[] labelAndArgs = message.split(" ");
    if (labelAndArgs.length <= 2 || !chCommandList.contains(labelAndArgs[0].toLowerCase())) {
      return;
    }

    if (lunaChatSubCommands.contains(labelAndArgs[1].toLowerCase(Locale.ROOT))) {
      return;
    }

    Channel ch = LunaChat.getAPI().getChannel(labelAndArgs[1]);
    if (ch == null) {
      return;
    }

    String chatMessage =
        e.getMessage().substring(labelAndArgs[0].length() + labelAndArgs[1].length() + 1);

    ChannelChatMessageData data =
        plugin
            .getMessageDataFactory()
            .createChannelChatMessageData(e.getPlayer(), ch.getName(), chatMessage);
    Bukkit.getScheduler()
        .runTaskAsynchronously(plugin, () -> plugin.getPublisher().publishChannelChatMessage(data));

    e.setCancelled(true);
  }
}
