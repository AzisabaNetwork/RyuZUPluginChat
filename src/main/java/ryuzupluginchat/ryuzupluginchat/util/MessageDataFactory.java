package ryuzupluginchat.ryuzupluginchat.util;

import com.github.ucchyocean.lc3.LunaChat;
import com.github.ucchyocean.lc3.LunaChatConfig;
import com.github.ucchyocean.lc3.channel.Channel;
import com.github.ucchyocean.lc3.japanize.JapanizeType;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import ryuzupluginchat.ryuzupluginchat.RyuZUPluginChat;
import ryuzupluginchat.ryuzupluginchat.useful.ColorUtils;
import ryuzupluginchat.ryuzupluginchat.useful.LuckPermsPrefixSuffixUtils;
import ryuzupluginchat.ryuzupluginchat.util.message.ChannelChatMessageData;
import ryuzupluginchat.ryuzupluginchat.util.message.GlobalMessageData;
import ryuzupluginchat.ryuzupluginchat.util.message.PrivateMessageData;
import ryuzupluginchat.ryuzupluginchat.util.message.SystemMessageData;

@RequiredArgsConstructor
public class MessageDataFactory {

  private final RyuZUPluginChat plugin;

  public GlobalMessageData createGlobalMessageData(Player p, String message) {
    return new GlobalMessageData(plugin.getRpcConfig().getChatFormat(), null,
        LuckPermsPrefixSuffixUtils.getPrefix(p), plugin.getPrefixSuffixContainer().getPrefix(p),
        plugin.getRpcConfig().getServerName(), null, p.getName(), p.getDisplayName(),
        plugin.getPrefixSuffixContainer().getSuffix(p), null,
        LuckPermsPrefixSuffixUtils.getSuffix(p), canJapanize(message, p), message, false,
        replaceMessage(message, p).replace("$", "").replace("#", ""));
  }

  public PrivateMessageData createPrivateMessageData(Player p, UUID targetUuid, String message) {
    return new PrivateMessageData(null, plugin.getRpcConfig().getServerName(), null, p.getName(),
        targetUuid, canJapanize(message, p), message,
        replaceMessage(message, p).replace("$", "").replace("#", ""));
  }

  public ChannelChatMessageData createChannelChatMessageData(Player p, String message) {
    Channel ch = LunaChat.getAPI().getDefaultChannel(p.getName());

    return new ChannelChatMessageData(ch.getName(), ch.getColorCode(),
        ch.getFormat(), null, LuckPermsPrefixSuffixUtils.getPrefix(p),
        plugin.getPrefixSuffixContainer().getPrefix(p), plugin.getRpcConfig().getServerName(), null,
        p.getName(), p.getDisplayName(), plugin.getPrefixSuffixContainer().getSuffix(p), null,
        LuckPermsPrefixSuffixUtils.getSuffix(p), canJapanize(message, p), message, false,
        replaceMessage(message, p).replace("$", "").replace("#", ""));
  }

  public SystemMessageData createGeneralSystemChatMessageData(String msg) {
    HashMap<String, Object> map = new HashMap<>();
    map.put("message", msg);

    map.put("type", SystemMessageType.GLOBAL_SYSTEM_MESSAGE.name());
    return new SystemMessageData(plugin.getRpcConfig().getServerName(), null, map);
  }

  public SystemMessageData createPrivateSystemChatMessageData(UUID target, String msg) {
    HashMap<String, Object> map = new HashMap<>();
    map.put("target", target.toString());
    map.put("message", msg);

    map.put("type", SystemMessageType.PRIVATE_SYSTEM_MESSAGE.name());
    return new SystemMessageData(plugin.getRpcConfig().getServerName(), null, map);
  }

  public SystemMessageData createImitationChatMessageData(UUID uuid, String msg) {
    HashMap<String, Object> map = new HashMap<>();
    map.put("imitateTo", uuid.toString());
    map.put("message", msg);

    map.put("type", SystemMessageType.IMITATION_CHAT.name());
    return new SystemMessageData(plugin.getRpcConfig().getServerName(), null, map);
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
