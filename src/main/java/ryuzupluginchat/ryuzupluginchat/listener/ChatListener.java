package ryuzupluginchat.ryuzupluginchat.listener;

import com.github.ucchyocean.lc3.LunaChat;
import com.github.ucchyocean.lc3.channel.Channel;
import java.util.HashMap;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import ryuzupluginchat.ryuzupluginchat.RyuZUPluginChat;
import ryuzupluginchat.ryuzupluginchat.message.data.ChannelChatMessageData;
import ryuzupluginchat.ryuzupluginchat.message.data.GlobalMessageData;

@RequiredArgsConstructor
public class ChatListener implements Listener {

  private final RyuZUPluginChat plugin;

  private HashMap<UUID, Long> lastDiscordChannelChatMap = new HashMap<>();

  @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
  public void onChat(AsyncPlayerChatEvent e) {
    Player p = e.getPlayer();
    boolean global = LunaChat.getAPI().getDefaultChannel(p.getName()) == null;
    if (global || e.getMessage().charAt(0) == '!' || e.getMessage().startsWith("#!")) {
      String msg = e.getMessage().substring(0, 2).replace("!", "") + e.getMessage().substring(2);
      GlobalMessageData data = plugin.getMessageDataFactory().createGlobalMessageData(p, msg);

      RyuZUPluginChat.newChain()
          .async(() -> plugin.getPublisher().publishGlobalMessage(data)).execute();
    } else {
      Channel channel = LunaChat.getAPI().getDefaultChannel(p.getName());
      if (channel.getName()
          .equals(plugin.getVcLunaChatChannelSharer().getLunaChatChannelName())) {
        if (lastDiscordChannelChatMap.getOrDefault(p.getUniqueId(), 0L) + 1000L
            > System.currentTimeMillis()) {
          e.setCancelled(true);
          p.sendMessage(ChatColor.RED + "クールタイム中です。1秒間待って実行してください。");
          return;
        }
        lastDiscordChannelChatMap.put(p.getUniqueId(), System.currentTimeMillis());
      }

      ChannelChatMessageData data = plugin.getMessageDataFactory()
          .createChannelChatMessageData(p, e.getMessage());
      RyuZUPluginChat.newChain()
          .async(() -> plugin.getPublisher().publishChannelChatMessage(data)).execute();
    }
    e.setFormat("");
    e.setCancelled(true);
  }
}
