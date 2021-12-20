package ryuzupluginchat.ryuzupluginchat;

import com.github.ucchyocean.lc3.LunaChat;
import com.github.ucchyocean.lc3.LunaChatAPI;
import com.github.ucchyocean.lc3.channel.Channel;
import com.github.ucchyocean.lc3.member.ChannelMemberBukkit;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ryuzupluginchat.ryuzupluginchat.util.SystemMessageType;
import ryuzupluginchat.ryuzupluginchat.util.message.ChannelChatMessageData;
import ryuzupluginchat.ryuzupluginchat.util.message.GlobalMessageData;
import ryuzupluginchat.ryuzupluginchat.util.message.PrivateMessageData;
import ryuzupluginchat.ryuzupluginchat.util.message.SystemMessageData;

@RequiredArgsConstructor
public class MessageProcessor {

  private final RyuZUPluginChat plugin;

  public void processGlobalMessage(GlobalMessageData data) {
    String message = data.format();

    for (Player p : Bukkit.getOnlinePlayers()) {
      p.sendMessage(message);
    }
  }

  public void processChannelChatMessage(ChannelChatMessageData data) {
    String message = data.format();

    LunaChatAPI api = LunaChat.getAPI();
    Channel channel = api.getChannel(data.getLunaChatChannelName());

    if (data.isFromDiscord()) {
      Bukkit.getOnlinePlayers().stream()
          .filter(p -> channel.getMembers().stream()
              .map(m -> ((ChannelMemberBukkit) m).getPlayer()).collect(Collectors.toList())
              .contains(p) || p.hasPermission("rpc.op"))
          .forEach(p -> p.sendMessage(message));
    } else {
      ChannelMemberBukkit member = ChannelMemberBukkit.getChannelMemberBukkit(data.getPlayerName());
      Bukkit.getOnlinePlayers().stream()
          .filter(p -> channel.getMembers().stream()
              .map(m -> ((ChannelMemberBukkit) m).getPlayer()).collect(Collectors.toList())
              .contains(p) || p.hasPermission("rpc.op"))
          .filter(p -> !api.getHidelist(ChannelMemberBukkit.getChannelMember((p.getName())))
              .contains(member))
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

    if (type == SystemMessageType.CHAT) {
      String msg = data.format((String) mapData.get("message"));
      Bukkit.broadcastMessage(msg);
    } else if (type == SystemMessageType.PLAYERS) {
      Map<String, String> preUUIDMap = convertObjectIntoMap(mapData.get("playerMap"));
      HashMap<String, UUID> uuidMap = new HashMap<>();
      for (String key : preUUIDMap.keySet()) {
        uuidMap.put(key, UUID.fromString(preUUIDMap.get(key)));
      }
      plugin.getTabCompletePlayerNameContainer().clearAndRegisterAll(uuidMap);
    } else if (type == SystemMessageType.PREFIX) {
      UUID uuid = UUID.fromString((String) mapData.get("uuid"));
      String prefix = (String) mapData.get("prefix");
      plugin.getPrefixSuffixContainer().setPrefix(uuid, prefix);
    } else if (type == SystemMessageType.SUFFIX) {
      UUID uuid = UUID.fromString((String) mapData.get("uuid"));
      String prefix = (String) mapData.get("suffix");
      plugin.getPrefixSuffixContainer().setPrefix(uuid, prefix);
    } else {
      // TODO error
    }
  }

  @SuppressWarnings("unchecked")
  private Map<String, String> convertObjectIntoMap(Object mapObject) {
    return (Map<String, String>) mapObject;
  }
}
