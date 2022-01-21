package ryuzupluginchat.ryuzupluginchat.message;

import com.github.ucchyocean.lc3.LunaChat;
import com.github.ucchyocean.lc3.LunaChatAPI;
import com.github.ucchyocean.lc3.channel.Channel;
import com.github.ucchyocean.lc3.member.ChannelMember;
import com.github.ucchyocean.lc3.member.ChannelMemberBukkit;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import ryuzupluginchat.ryuzupluginchat.RyuZUPluginChat;
import ryuzupluginchat.ryuzupluginchat.message.data.ChannelChatMessageData;
import ryuzupluginchat.ryuzupluginchat.message.data.GlobalMessageData;
import ryuzupluginchat.ryuzupluginchat.message.data.PrivateMessageData;
import ryuzupluginchat.ryuzupluginchat.message.data.SystemMessageData;

@RequiredArgsConstructor
public class MessageProcessor {

  private final RyuZUPluginChat plugin;

  public void processGlobalMessage(GlobalMessageData data) {
    String message = data.format();

    LunaChatAPI api = LunaChat.getAPI();
    ChannelMember sender = ChannelMemberBukkit.getChannelMember(data.getPlayerName());

    Bukkit.getOnlinePlayers().stream()
        .filter(p -> sender == null ||
            !api.getHideinfo(ChannelMemberBukkit.getChannelMember(p.getUniqueId()))
                .contains(sender))
        .forEach((p) -> p.sendMessage(message));

    plugin.getLogger().info("[Global-Chat] " + ChatColor.stripColor(message));
  }

  public void processChannelChatMessage(ChannelChatMessageData data) {
    String message = data.format();

    LunaChatAPI api = LunaChat.getAPI();
    Channel channel = api.getChannel(data.getLunaChatChannelName());

    if (channel == null) {
      plugin.getLogger()
          .warning("[Chat] The channel named " + data.getLunaChatChannelName() + " was not found.");
      return;
    }

    plugin.getLogger()
        .info("[Channel-Chat] (" + channel.getName() + ") " + ChatColor.stripColor(message));

    if (data.isFromDiscord()) {
      Bukkit.getOnlinePlayers().stream()
          .filter(p -> channel.getMembers().stream()
              .map(m -> ((ChannelMemberBukkit) m).getPlayer()).collect(Collectors.toList())
              .contains(p) || p.hasPermission("rpc.op"))
          .forEach(p -> p.sendMessage(message));
    } else {
      ChannelMemberBukkit sender = ChannelMemberBukkit.getChannelMemberBukkit(data.getPlayerName());
      Bukkit.getOnlinePlayers().stream()
          .filter(p ->
              channel.getMembers().stream()
                  .map(m -> ((ChannelMemberBukkit) m).getPlayer())
                  .collect(Collectors.toList()).contains(p)
                  || p.hasPermission("rpc.op"))
          .filter(p ->
              !api.getHideinfo(ChannelMemberBukkit.getChannelMember(p.getUniqueId()))
                  .contains(sender))
          .forEach(p -> p.sendMessage(message));
    }
  }

  public void processPrivateMessage(PrivateMessageData data) {
    String message = data.format();

    Player targetPlayer = Bukkit.getPlayer(data.getReceivedPlayerUUID());
    if (targetPlayer == null) {
      return;
    }

    targetPlayer.sendMessage(message);
    plugin.getLogger().info("[Private-Chat] " + ChatColor.stripColor(message));

    RyuZUPluginChat.newChain()
        .async(() -> {
          UUID uuid = plugin.getPlayerUUIDMapContainer().getUUID(data.getSentPlayerName());
          if (uuid == null) {
            return;
          }
          plugin.getReplyTargetFetcher().setReplyTarget(targetPlayer, uuid);
        })
        .async(() -> {
              plugin.getPublisher().notifyPrivateChatReached(data.getId(), plugin.getRpcConfig()
                  .getServerName(), targetPlayer.getName());
            }
        ).execute();
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
