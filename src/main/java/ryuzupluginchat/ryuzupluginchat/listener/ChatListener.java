package ryuzupluginchat.ryuzupluginchat.listener;

import com.github.ucchyocean.lc3.LunaChat;
import com.github.ucchyocean.lc3.LunaChatConfig;
import com.github.ucchyocean.lc3.japanize.JapanizeType;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import ryuzupluginchat.ryuzupluginchat.RyuZUPluginChat;
import ryuzupluginchat.ryuzupluginchat.util.ColorUtils;
import ryuzupluginchat.ryuzupluginchat.util.LuckPermsPrefixSuffixUtils;
import ryuzupluginchat.ryuzupluginchat.util.message.GlobalMessageData;

@RequiredArgsConstructor
public class ChatListener implements Listener {

  private final RyuZUPluginChat plugin;

  @EventHandler
  public void onChat(AsyncPlayerChatEvent e) {
    if (!e.isCancelled()) {
      Player p = e.getPlayer();
      boolean global = LunaChat.getAPI().getDefaultChannel(p.getName()) == null;
      if (global || e.getMessage().charAt(0) == '!') {

        // 最初の文字が ! の場合は ! を除外する
        String msg = e.getMessage().charAt(0) == '!' ? e.getMessage().substring(1) : e.getMessage();

        sendGlobalChat(p, msg);
      } else {
        // TODO: channelChatの時に送る
        // sendChannelMessage(p, e.getMessage(), LunaChat.getAPI().getDefaultChannel(p.getName()));
      }
      e.setFormat("");
      e.setCancelled(true);
    }
  }

  private boolean sendGlobalChat(Player p, String message) {
    GlobalMessageData data = new GlobalMessageData(plugin.getRpcConfig().getChatFormat(), null,
        LuckPermsPrefixSuffixUtils.getPrefix(p), plugin.getPrefixSuffixContainer().getPrefix(p),
        plugin.getRpcConfig().getServerName(), null, p.getName(), p.getDisplayName(),
        plugin.getPrefixSuffixContainer().getSuffix(p), null,
        LuckPermsPrefixSuffixUtils.getSuffix(p), canJapanize(message, p), message, false,
        replaceMessage(message, p).replace("$", "").replace("#", ""));

    plugin.getPublisher().publishGlobalMessage(data);
    return true;
  }

  private boolean canJapanize(String msg, Player p) {
    LunaChatConfig config = LunaChat.getConfig();
    return LunaChat.getAPI().isPlayerJapanize(p.getName()) &&
        config.getJapanizeType() != JapanizeType.NONE &&
        msg.getBytes(StandardCharsets.UTF_8).length <= msg.length() &&
        msg.charAt(0) != '#' && msg.charAt(0) != '$';
  }

  private String replaceMessage(String msg, Player p) {
    String message = msg;
    LunaChatConfig config = LunaChat.getConfig();
    if (canJapanize(msg, p)) {
      message = LunaChat.getAPI().japanize(message, config.getJapanizeType());
    }
    if (config.isEnableNormalChatColorCode() || p.hasPermission("lunachat.allowcc")) {
      message = ColorUtils.setColor(message);
    }
    return message;
  }

}
