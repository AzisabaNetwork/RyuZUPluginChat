package net.azisaba.ryuzupluginchat.discord.deliverer;

import com.github.ucchyocean.lc3.LunaChat;
import com.github.ucchyocean.lc3.LunaChatAPI;
import com.github.ucchyocean.lc3.channel.Channel;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import net.azisaba.ryuzupluginchat.RyuZUPluginChat;
import net.azisaba.ryuzupluginchat.discord.data.ChannelChatSyncData;
import net.azisaba.ryuzupluginchat.message.data.ChannelChatMessageData;
import net.azisaba.ryuzupluginchat.message.data.GlobalMessageData;
import org.bukkit.Bukkit;

@RequiredArgsConstructor
public class DiscordMessageDeliverer {

  private final RyuZUPluginChat plugin;

  public void sendToGlobal(MessageCreateEvent event) {
    Message message = event.getMessage();
    String content = message.getContent();
    if (content.length() <= 0) {
      return;
    }
    content = removeUrl(content);

    Member messageAuthor = message.getAuthorAsMember().block();
    if (messageAuthor == null) {
      return;
    }

    String senderName = messageAuthor.getNickname().orElse(messageAuthor.getUsername());

    GlobalMessageData data =
        plugin.getMessageDataFactory().createGlobalMessageDataFromDiscord(senderName, content);

    plugin.getPublisher().publishGlobalMessage(data);
  }

  public void sendToChannel(MessageCreateEvent event, ChannelChatSyncData syncData) {
    Message message = event.getMessage();
    String content = message.getContent();
    if (content.length() <= 0) {
      return;
    }
    final String urlDeletedContent = removeUrl(content);

    Member messageAuthor = message.getAuthorAsMember().block();
    if (messageAuthor == null) {
      return;
    }

    String senderName = messageAuthor.getNickname().orElse(messageAuthor.getUsername());

    Bukkit.getScheduler()
        .runTaskAsynchronously(
            plugin,
            () -> {
              LunaChatAPI api = LunaChat.getAPI();
              List<Channel> channelList = new ArrayList<>();
              for (Channel channel : api.getChannels()) {
                if (syncData.isMatch(channel.getName())) {
                  channelList.add(channel);
                }
              }

              for (Channel ch : channelList) {
                ChannelChatMessageData data =
                    plugin
                        .getMessageDataFactory()
                        .createChannelChatMessageDataFromDiscord(
                            senderName, ch.getName(), urlDeletedContent);

                plugin.getPublisher().publishChannelChatMessage(data);
              }
            });
  }

  private String removeUrl(String msg) {
    String urlPattern =
        "((https?|ftp|gopher|telnet|file|Unsure|http):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)";
    Pattern p = Pattern.compile(urlPattern, Pattern.CASE_INSENSITIVE);
    Matcher m = p.matcher(msg);
    int i = 0;
    while (m.find()) {
      msg = msg.replaceAll(m.group(i), "<URL>").trim();
      i++;
    }
    return msg;
  }
}
