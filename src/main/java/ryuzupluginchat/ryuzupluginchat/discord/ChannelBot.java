package ryuzupluginchat.ryuzupluginchat.discord;

import com.google.gson.Gson;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.MessageChannel;
import java.util.HashMap;
import java.util.Map;

public class ChannelBot extends ChatLogBot {

  public String channelname;

  public ChannelBot(String token, Long channnel, String GroupName, String channelname) {
    super(token, channnel, GroupName, BotType.Channel);
    this.channelname = channelname;

    MessageEvent.dispose();
    MessageEvent = gateway.on(MessageCreateEvent.class).subscribe(event -> {
      if (channel.getId().asLong() != event.getMessage().getChannelId().asLong()
          || event.getMessage().getAuthor().get().isBot()) {
        return;
      }
      MessageChannel channel1 = event.getMessage().getChannel().block();
      if (channel1 == null) {
        return;
      }
      if (channel1.equals(channel)) {
        Map<String, String> map = new HashMap<>();
        Gson gson = new Gson();
        map.put("System", "Chat");
        map.put("ChannelName", channelname);
        map.put("Message", event.getMessage().getContent());
        map.put("Discord", event.getMessage().getAuthor().get().getUsername());
        RyuZUBungeeChat.ServerGroups.get(GroupName).servers.forEach(
            s -> RyuZUBungeeChat.RBC.sendPluginMessage(s, "ryuzuchat:ryuzuchat", gson.toJson(map)));
        RyuZUBungeeChat.ServerGroups.get(GroupName).adminbot.sendLogMessage(map,
            SendType.DiscordChannel);
      }
    });
  }
}