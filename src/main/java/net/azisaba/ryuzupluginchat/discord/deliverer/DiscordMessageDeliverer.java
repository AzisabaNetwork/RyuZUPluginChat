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

    public void sendToGlobal(MessageReceivedEvent event){
        // get data from event
        Message message = event.getMessage();
        String content = message.getContentStripped();
        if(content.isEmpty()) return;

        // sanitize content
        content = removeUrl(content);

        // get username
        User author = message.getAuthor();
        String senderName = author.getEffectiveName();

        // create data and publish
        GlobalMessageData data = plugin.getMessageDataFactory()
                .createGlobalMessageDataFromDiscord(senderName, content);
        plugin.getPublisher().publishGlobalMessage(data);
    }

    public void sendToChannel(MessageReceivedEvent event, ChannelChatSyncData syncData) {
        Message message = event.getMessage();
        String content = message.getContentStripped();
        if(content.isEmpty()) return;

        final String sanitizedContent = removeUrl(content);

        User author = message.getAuthor();
        String senderName = author.getEffectiveName();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            LunaChat.getAPI().getChannels().stream()
                    .filter(ch -> syncData.isMatch(ch.getName()))
                    .forEach(ch -> {
                        ChannelChatMessageData data = plugin.getMessageDataFactory()
                                .createChannelChatMessageDataFromDiscord(senderName, ch.getName(), sanitizedContent);
                        plugin.getPublisher().publishChannelChatMessage(data);
                    });
        });
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
