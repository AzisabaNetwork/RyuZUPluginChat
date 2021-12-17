package ryuzupluginchat.ryuzupluginchat;

import com.github.ucchyocean.lc3.LunaChat;
import com.github.ucchyocean.lc3.LunaChatAPI;
import com.github.ucchyocean.lc3.channel.Channel;
import com.github.ucchyocean.lc3.member.ChannelMemberBukkit;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ryuzupluginchat.ryuzupluginchat.util.message.ChannelChatMessageData;
import ryuzupluginchat.ryuzupluginchat.util.message.GlobalMessageData;
import ryuzupluginchat.ryuzupluginchat.util.message.PrivateMessageData;
import ryuzupluginchat.ryuzupluginchat.util.message.SystemMessageData;

public class MessageProcessor {

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

    Player targetPlayer = Bukkit.getPlayerExact(data.getReceivedPlayerName());
    if (targetPlayer == null) {
      return;
    }

    targetPlayer.sendMessage(message);
  }

  public void processSystemMessage(SystemMessageData data) {
    String message = data.format();
    Bukkit.getOnlinePlayers().forEach(p -> p.sendMessage(message));
  }
}
