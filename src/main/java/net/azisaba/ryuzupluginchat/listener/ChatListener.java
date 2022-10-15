package net.azisaba.ryuzupluginchat.listener;

import com.github.ucchyocean.lc3.LunaChat;
import com.github.ucchyocean.lc3.channel.Channel;
import java.util.HashMap;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import net.azisaba.ryuzupluginchat.RyuZUPluginChat;
import net.azisaba.ryuzupluginchat.message.data.ChannelChatMessageData;
import net.azisaba.ryuzupluginchat.message.data.GlobalMessageData;
import net.azisaba.ryuzupluginchat.util.Chat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

@RequiredArgsConstructor
public class ChatListener implements Listener {

  private final RyuZUPluginChat plugin;

  private final HashMap<UUID, Long> lastDiscordChannelChatMap = new HashMap<>();

  @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
  public void onChat(AsyncPlayerChatEvent e) {
    Player p = e.getPlayer();
    boolean global = LunaChat.getAPI().getDefaultChannel(p.getName()) == null;

    if (global || e.getMessage().charAt(0) == '!' || e.getMessage().startsWith("#!")) {
      String msg = cutPrefix(e.getMessage());
      GlobalMessageData data = plugin.getMessageDataFactory().createGlobalMessageData(p, msg);

      Bukkit.getScheduler()
          .runTaskAsynchronously(plugin, () -> plugin.getPublisher().publishGlobalMessage(data));
    } else {
      Channel channel = LunaChat.getAPI().getDefaultChannel(p.getName());
      if (channel.getName().equals(plugin.getVcLunaChatChannelSharer().getLunaChatChannelName())) {
        if (lastDiscordChannelChatMap.getOrDefault(p.getUniqueId(), 0L) + 1000L
            > System.currentTimeMillis()) {
          e.setCancelled(true);
          p.sendMessage(ChatColor.RED + "クールタイム中です。1秒間待って実行してください。");
          return;
        }
        lastDiscordChannelChatMap.put(p.getUniqueId(), System.currentTimeMillis());
      }

      Channel ch = LunaChat.getAPI().getDefaultChannel(p.getName());

      if (!p.hasPermission("lunachat.speak." + ch.getName())) {
        p.sendMessage(Chat.f("&cこのチャンネルでメッセージを送信する権限がありません！"));
        return;
      }

      ChannelChatMessageData data =
          plugin
              .getMessageDataFactory()
              .createChannelChatMessageData(p, ch.getName(), e.getMessage());

      Bukkit.getScheduler()
          .runTaskAsynchronously(
              plugin, () -> plugin.getPublisher().publishChannelChatMessage(data));
    }
    e.setFormat("");
    e.setCancelled(true);
  }

  private String cutPrefix(String msg) {
    if (msg.length() >= 2) {
      return msg.substring(0, 2).replace("!", "") + msg.substring(2);
    } else {
      return msg.replace("!", "");
    }
  }
}
