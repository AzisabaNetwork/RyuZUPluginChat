package ryuzupluginchat.ryuzupluginchat.discord;

import com.google.gson.Gson;
import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.MessageChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.md_5.bungee.api.ChatColor;
import reactor.core.Disposable;
import reactor.util.annotation.NonNull;

public class ChatLogBot {

  public enum BotType {Admin, Member, Channel}

  public enum SendType {Global, Channel, Private, Discord, DiscordChannel}

  public GatewayDiscordClient gateway;
  public MessageChannel channel;
  public Disposable MessageEvent;
  public String GroupName;

  public ChatLogBot(@NonNull String token, @NonNull Long channnel, @NonNull String GroupName,
      BotType type) {
    DiscordClient client = DiscordClient.create(token);
    gateway = client.login().block();
    channel = (MessageChannel) gateway.getChannelById(Snowflake.of(channnel)).block();
    this.GroupName = GroupName;

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
        map.put("Message", event.getMessage().getContent());
        map.put("Discord", event.getMessage().getAuthor().get().getUsername());
        RyuZUBungeeChat.ServerGroups.get(GroupName).servers.forEach(
            s -> RyuZUBungeeChat.RBC.sendPluginMessage(s, "ryuzuchat:ryuzuchat", gson.toJson(map)));
        if (type.equals(BotType.Member)) {
          RyuZUBungeeChat.ServerGroups.get(GroupName).adminbot.sendLogMessage(map,
              SendType.Discord);
        }
      }
    });
  }

  public void sendLogMessage(Map<String, String> map, SendType type) {
    String msg = null;
    if (type.equals(SendType.Global)) {
      msg = setFormat(map);
      msg = "(" + map.get("SendServerName") + ")" + " --> " + msg;
    } else if (type.equals(SendType.Private)) {
      msg = setFormat(map);
      msg = "(" + map.get("SendServerName") + ")" + " --> " + msg + " --> " +
          map.getOrDefault("ReceivedPlayerLuckPermsPrefix", "") +
          map.getOrDefault("ReceivedPlayerRyuZUMapPrefix", "") +
          map.getOrDefault("ReceivedPlayerName", "") +
          map.getOrDefault("ReceivedPlayerLuckPermsSuffix", "") +
          map.getOrDefault("ReceivedPlayerRyuZUMapSuffix", "") +
          " --> " + "(" + map.get("ReceiveServerName") + ")";
    } else if (type.equals(SendType.Channel)) {
      msg = setFormat(map);
      msg = "(" + map.get("SendServerName") + ")" + "[" + map.get("ChannelName") + "]" + " --> "
          + msg;
    } else if (type.equals(SendType.Discord)) {
      msg = "[" + "Discord" + "]" + map.get("Discord") + " " + map.get("Message");
    } else if (type.equals(SendType.DiscordChannel)) {
      msg = "[" + "Discord" + "]" + "(" + map.get("ChannelName") + ")" + map.get("Discord") + " "
          + map.get("Message");
    }

    msg = msg.replace("@", "＠");
    msg = msg.replace("__", "\\__");
    msg = msg.replace("_", "\\_");

    channel.createMessage(ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', msg)))
        .block();
  }

  private String setFormat(Map<String, String> map) {
    String msg = map.get("Format");
    msg = msg.replace("[LuckPermsPrefix]", map.getOrDefault("LuckPermsPrefix", ""))
        .replace("[LunaChatPrefix]", map.getOrDefault("LunaChatPrefix", ""))
        .replace("[RyuZUMapPrefix]", map.getOrDefault("RyuZUMapPrefix", ""))
        .replace("[SendServerName]", map.getOrDefault("SendServerName", ""))
        .replace("[ReceiveServerName]", map.getOrDefault("ReceiveServerName", ""))
        .replace("[PlayerName]", map.getOrDefault("PlayerName", ""))
        .replace("[RyuZUMapSuffix]", map.getOrDefault("RyuZUMapSuffix", ""))
        .replace("[LunaChatSuffix]", map.getOrDefault("LunaChatSuffix", ""))
        .replace("[LuckPermsSuffix]", map.getOrDefault("LuckPermsSuffix", ""));
    msg = setColor(msg);
    msg = msg.replace("[PreReplaceMessage]",
            (Boolean.parseBoolean(map.get("CanJapanese")) ? "(" + map.get("PreReplaceMessage") + ")"
                : ""))
        .replace("[Message]", map.getOrDefault("Message", ""));
    return msg;
  }

  private String setColor(String msg) {
    String replaced = msg;
    replaced = replaceToHexFromRGB(replaced);
    return replaced;
  }

  private String replaceToHexFromRGB(String text) {
    String regex = "\\{.+?}";
    List<String> RGBcolors = new ArrayList<>();
    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(text);
    while (matcher.find()) {
      RGBcolors.add(matcher.group());
    }
    for (String hexcolor : RGBcolors) {
      text = text.replace(hexcolor, "");
    }
    return text;
  }
}