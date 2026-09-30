package net.azisaba.ryuzupluginchat.discord.deliverer;

import com.github.ucchyocean.lc3.LunaChat;
import lombok.RequiredArgsConstructor;
import net.azisaba.ryuzupluginchat.RyuZUPluginChat;
import net.azisaba.ryuzupluginchat.discord.data.ChannelChatSyncData;
import net.azisaba.ryuzupluginchat.message.data.ChannelChatMessageData;
import net.azisaba.ryuzupluginchat.message.data.GlobalMessageData;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.bukkit.Bukkit;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public class DiscordMessageDeliverer {

  private final RyuZUPluginChat plugin;

  public void sendToGlobal(MessageReceivedEvent event) {
    Message message = event.getMessage();
    String content = message.getContentStripped();
    if (content.isEmpty()) {
      return;
    }
    String sanitizedContent = removeUrl(content);

    String senderName = getSenderName(event);

    GlobalMessageData data =
        plugin.getMessageDataFactory().createGlobalMessageDataFromDiscord(senderName, sanitizedContent);

    plugin.getPublisher().publishGlobalMessage(data);
  }

  public void sendToChannel(MessageReceivedEvent event, ChannelChatSyncData syncData) {
    Message message = event.getMessage();
    String content = message.getContentStripped();
    if (content.isEmpty()) {
      return;
    }
    String sanitizedContent = removeUrl(content);

    String senderName = getSenderName(event);

    // LunaChat is not thread-safe, so touch its API from an async Bukkit task.
    Bukkit.getScheduler()
        .runTaskAsynchronously(
            plugin,
            () -> {
              LunaChat.getAPI().getChannels().stream()
                  .filter(ch -> syncData.isMatch(ch.getName()))
                  .forEach(
                      ch -> {
                        ChannelChatMessageData data =
                            plugin
                                .getMessageDataFactory()
                                .createChannelChatMessageDataFromDiscord(
                                    senderName, ch.getName(), sanitizedContent);

                        plugin.getPublisher().publishChannelChatMessage(data);
                      });
            });
  }

  // Prefer the guild nickname, fallback to the user's display name (global name or username).
  private String getSenderName(MessageReceivedEvent event) {
    User author = event.getAuthor();
    if (event.getMember() != null) {
      return event.getMember().getEffectiveName();
    }
    return author.getEffectiveName();
  }

  private String removeUrl(String msg) {
    String urlPattern =
        "((https?|ftp|gopher|telnet|file):((//)|(\\\\))+[\\w:#@%/;$()~_?+\\-=\\\\.&]*)";
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
