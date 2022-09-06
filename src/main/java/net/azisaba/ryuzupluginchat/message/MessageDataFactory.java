package net.azisaba.ryuzupluginchat.message;

import com.github.ucchyocean.lc3.LunaChat;
import com.github.ucchyocean.lc3.LunaChatConfig;
import com.github.ucchyocean.lc3.channel.Channel;
import com.github.ucchyocean.lc3.japanize.JapanizeType;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import net.azisaba.ryuzupluginchat.RyuZUPluginChat;
import net.azisaba.ryuzupluginchat.message.data.ChannelChatMessageData;
import net.azisaba.ryuzupluginchat.message.data.GlobalMessageData;
import net.azisaba.ryuzupluginchat.message.data.PrivateMessageData;
import net.azisaba.ryuzupluginchat.message.data.SystemMessageData;
import net.azisaba.ryuzupluginchat.util.LuckPermsPrefixSuffixUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public class MessageDataFactory {

  private final RyuZUPluginChat plugin;

  public GlobalMessageData createGlobalMessageData(Player p, String message) {
    String fixedMessage = message;
    if (message.startsWith("#")) {
      fixedMessage = message.substring(1);
    }

    return new GlobalMessageData(
        plugin.getRpcConfig().getGlobalChatFormat(),
        null,
        LuckPermsPrefixSuffixUtils.getPrefix(p),
        plugin.getPrefixSuffixContainer().getPrefix(p),
        plugin.getRpcConfig().getServerName(),
        null,
        p.getUniqueId(),
        p.getName(),
        p.getDisplayName(),
        plugin.getPrefixSuffixContainer().getSuffix(p),
        null,
        LuckPermsPrefixSuffixUtils.getSuffix(p),
        canJapanize(message, p),
        fixedMessage,
        false,
        replaceMessage(message, p));
  }

  public GlobalMessageData createGlobalMessageDataFromDiscord(String userName, String message) {
    return new GlobalMessageData(
        plugin.getRpcConfig().getGlobalChatFormat(),
        null,
        null,
        null,
        "Discord",
        null,
        null,
        userName,
        userName,
        null,
        null,
        null,
        false,
        message,
        true,
        message);
  }

  public PrivateMessageData createPrivateMessageData(Player p, UUID targetUuid, String message) {
    String fixedMessage = message;
    if (message.startsWith("#")) {
      fixedMessage = message.substring(1);
    }

    long id = plugin.getPrivateChatIDGetter().getNewId();
    return new PrivateMessageData(
        id,
        plugin.getRpcConfig().getPrivateChatFormat(),
        plugin.getRpcConfig().getServerName(),
        null,
        p.getName(),
        p.getDisplayName(),
        p.getUniqueId(),
        null,
        null,
        targetUuid,
        canJapanize(message, p),
        fixedMessage,
        replaceMessage(message, p));
  }

  /**
   * @deprecated Use {@link #createChannelChatMessageData(Player, String, String)} instead.
   */
  @Deprecated
  public ChannelChatMessageData createChannelChatMessageData(Player p, String message) {
    Channel ch = LunaChat.getAPI().getDefaultChannel(p.getName());
    return createChannelChatMessageData(p, ch.getName(), message);
  }

  public ChannelChatMessageData createChannelChatMessageData(
      Player p, String lunaChatChannel, String message) {
    Channel ch = LunaChat.getAPI().getChannel(lunaChatChannel);

    String fixedMessage = message;
    if (message.startsWith("#")) {
      fixedMessage = message.substring(1);
    }

    return new ChannelChatMessageData(
        ch.getName(),
        ch.getColorCode(),
        ch.getFormat(),
        null,
        LuckPermsPrefixSuffixUtils.getPrefix(p),
        plugin.getPrefixSuffixContainer().getPrefix(p),
        plugin.getRpcConfig().getServerName(),
        null,
        p.getUniqueId(),
        p.getName(),
        p.getDisplayName(),
        plugin.getPrefixSuffixContainer().getSuffix(p),
        null,
        LuckPermsPrefixSuffixUtils.getSuffix(p),
        canJapanize(message, p),
        fixedMessage,
        false,
        replaceMessage(message, p));
  }

  public ChannelChatMessageData createChannelChatMessageDataFromDiscord(
      String userName, String lunaChatChannel, String message) {
    Channel ch = LunaChat.getAPI().getChannel(lunaChatChannel);

    return new ChannelChatMessageData(
        ch.getName(),
        ch.getColorCode(),
        ch.getFormat(),
        null,
        null,
        null,
        "Discord",
        null,
        null,
        userName,
        userName,
        null,
        null,
        null,
        false,
        message,
        true,
        message);
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
    map.put("message", ChatColor.translateAlternateColorCodes('&', msg));

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
    if (!LunaChat.getAPI().isPlayerJapanize(p.getName())) {
      return false;
    }
    if (config.getJapanizeType() == JapanizeType.NONE) {
      return false;
    }
    if (msg.getBytes(StandardCharsets.UTF_8).length > msg.length()) {
      return false;
    }

    return !msg.startsWith("#");
  }

  private String replaceMessage(String msg, Player p) {
    String message = msg;
    LunaChatConfig config = LunaChat.getConfig();
    if (canJapanize(msg, p)) {
      message = LunaChat.getAPI().japanize(message, config.getJapanizeType());
    }
    if (message.startsWith("#")) {
      message = message.substring(1);
    }
    if (p.hasPermission("lunachat.allowcc")) {
      message = ChatColor.translateAlternateColorCodes('&', message);
    }
    return message;
  }
}
