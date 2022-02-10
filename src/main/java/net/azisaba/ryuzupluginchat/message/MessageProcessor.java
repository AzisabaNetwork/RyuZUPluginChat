package net.azisaba.ryuzupluginchat.message;

import com.github.ucchyocean.lc3.LunaChat;
import com.github.ucchyocean.lc3.LunaChatAPI;
import com.github.ucchyocean.lc3.channel.Channel;
import com.github.ucchyocean.lc3.member.ChannelMemberBukkit;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import net.azisaba.ryuzupluginchat.RyuZUPluginChat;
import net.azisaba.ryuzupluginchat.message.data.ChannelChatMessageData;
import net.azisaba.ryuzupluginchat.message.data.GlobalMessageData;
import net.azisaba.ryuzupluginchat.message.data.PrivateMessageData;
import net.azisaba.ryuzupluginchat.message.data.SystemMessageData;
import net.azisaba.ryuzupluginchat.util.Chat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public class MessageProcessor {

  private final RyuZUPluginChat plugin;

  public void processGlobalMessage(GlobalMessageData data) {
    String message = data.format();

    UUID senderUUID = plugin.getPlayerUUIDMapContainer().getUUID(data.getPlayerName());
    final Set<UUID> deafenPlayers;
    if (senderUUID != null) {
      deafenPlayers = plugin.getHideInfoController().getPlayersWhoHide(senderUUID);
    } else {
      deafenPlayers = Collections.emptySet();
    }

    Bukkit.getOnlinePlayers().stream()
        .filter(p -> !deafenPlayers.contains(p.getUniqueId()))
        .forEach((p) -> p.sendMessage(message));

    plugin.getLogger().info("[Global-Chat] " + ChatColor.stripColor(message));
  }

  public void processChannelChatMessage(ChannelChatMessageData data) {
    String message = data.format();

    LunaChatAPI api = LunaChat.getAPI();
    Channel channel = api.getChannel(data.getLunaChatChannelName());

    if (channel == null) {
      plugin
          .getLogger()
          .warning(
              Chat.f("[Chat] The channel named {0} is not found.", data.getLunaChatChannelName()));
      return;
    }

    plugin
        .getLogger()
        .info(Chat.f("[Channel-Chat] ({0}) {1}", channel.getName(), ChatColor.stripColor(message)));

    if (data.isFromDiscord()) {
      Bukkit.getOnlinePlayers().stream()
          .filter(
              p -> {
                if (channel.getMembers().stream()
                    .map(m -> ((ChannelMemberBukkit) m).getPlayer())
                    .collect(Collectors.toList())
                    .contains(p)) {
                  return true;
                }
                return p.hasPermission("rpc.op");
              })
          .forEach(p -> p.sendMessage(message));

    } else {
      UUID senderUUID = plugin.getPlayerUUIDMapContainer().getUUID(data.getPlayerName());
      final Set<UUID> deafenPlayers;
      if (senderUUID != null) {
        deafenPlayers = plugin.getHideInfoController().getPlayersWhoHide(senderUUID);
      } else {
        deafenPlayers = Collections.emptySet();
      }

      Bukkit.getOnlinePlayers().stream()
          .filter(
              p -> {
                if (channel.getMembers().stream()
                    .map(m -> ((ChannelMemberBukkit) m).getPlayer())
                    .collect(Collectors.toList())
                    .contains(p)) {
                  return true;
                }
                return p.hasPermission("rpc.op");
              })
          .filter(p -> !deafenPlayers.contains(p.getUniqueId()))
          .forEach(p -> p.sendMessage(message));
    }
  }

  public void processPrivateMessage(PrivateMessageData data) {
    String receiverName =
        plugin.getPlayerUUIDMapContainer().getNameFromUUID(data.getReceivedPlayerUUID());
    if (receiverName != null) {
      data.setReceivedPlayerName(receiverName);
    }
    String message = data.format();

    if (receiverName != null) {
      Bukkit.getOnlinePlayers().stream()
          .filter(p -> p.hasPermission("rpc.op"))
          .filter(
              p ->
                  !p.getUniqueId().equals(data.getReceivedPlayerUUID())
                      && !p.getName().equalsIgnoreCase(data.getSentPlayerName()))
          .forEach(p -> p.sendMessage(message));
    }

    Player targetPlayer = Bukkit.getPlayer(data.getReceivedPlayerUUID());
    if (targetPlayer == null) {
      return;
    }

    UUID senderUUID = plugin.getPlayerUUIDMapContainer().getUUID(data.getSentPlayerName());
    if (!plugin.getHideInfoController().isHidingPlayer(targetPlayer.getUniqueId(), senderUUID)) {
      targetPlayer.sendMessage(message);
    }
    plugin.getLogger().info("[Private-Chat] " + ChatColor.stripColor(message));

    RyuZUPluginChat.newChain()
        .async(
            () -> {
              if (senderUUID == null) {
                return;
              }
              plugin.getReplyTargetFetcher().setReplyTarget(targetPlayer, senderUUID);
            })
        .async(
            () ->
                plugin
                    .getPublisher()
                    .notifyPrivateChatReached(
                        data.getId(),
                        plugin.getRpcConfig().getServerName(),
                        targetPlayer.getName()))
        .execute();
  }

  public void processSystemMessage(SystemMessageData data) {
    Map<String, Object> mapData = data.getMap();
    SystemMessageType type;
    try {
      type = SystemMessageType.valueOf((String) mapData.get("type"));
    } catch (IllegalArgumentException e) {
      // TODO error
      return;
    }

    if (type == SystemMessageType.GLOBAL_SYSTEM_MESSAGE) {
      String msg = data.format((String) mapData.get("message"));
      Bukkit.broadcastMessage(msg);

    } else if (type == SystemMessageType.PRIVATE_SYSTEM_MESSAGE) {
      String msg = data.format((String) mapData.get("message"));
      UUID target = UUID.fromString((String) mapData.get("target"));
      Player targetPlayer = Bukkit.getPlayer(target);
      if (targetPlayer == null) {
        return;
      }
      targetPlayer.sendMessage(msg);

    } else if (type == SystemMessageType.IMITATION_CHAT) {
      String msg = data.format((String) mapData.get("message"));
      UUID target = UUID.fromString((String) mapData.get("imitateTo"));
      Player targetPlayer = Bukkit.getPlayer(target);
      if (targetPlayer == null) {
        return;
      }
      boolean global = LunaChat.getAPI().getDefaultChannel(targetPlayer.getName()) == null;
      if (global || msg.charAt(0) == '!') {
        // TODO send global message from targetPlayer
      } else {
        // TODO send channel chat message from targetPlayer
      }
    } else {
      // TODO error
    }
  }

  @SuppressWarnings("unchecked")
  private Map<String, String> convertObjectIntoMap(Object mapObject) {
    return (Map<String, String>) mapObject;
  }
}
