package net.azisaba.ryuzupluginchat.discord.deliverer;

import lombok.RequiredArgsConstructor;
import net.azisaba.ryuzupluginchat.RyuZUPluginChat;
import net.azisaba.ryuzupluginchat.message.data.ChannelChatMessageData;
import net.azisaba.ryuzupluginchat.message.data.GlobalMessageData;
import net.azisaba.ryuzupluginchat.message.data.PrivateMessageData;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import org.bukkit.ChatColor;

@RequiredArgsConstructor
public class JDAServerChatMessageDeliverer {
    private final RyuZUPluginChat plugin;
    private final JDA client;

    public void sendToDiscord(GlobalMessageData data, MessageChannel targetChannel, boolean vcMode) {
        String message = sanitize(vcMode ? data.getMessage() : data.format());
        targetChannel.sendMessage(message).queue(sentMessage -> {
            if (vcMode) {
                String editedMessageContent = sanitize(data.format());
                sentMessage.editMessage(editedMessageContent).queue();
            }
        }, throwable -> {
            plugin.getSLF4JLogger().error("Failed to send message to Discord", throwable);
        });
    }

    public void sendToDiscord(ChannelChatMessageData data, MessageChannel targetChannel, boolean vcMode) {
        final String message = sanitize(vcMode ? data.getMessage() : data.format());
        targetChannel.sendMessage(message).queue(sentMessage -> {
            if (vcMode) {
                String editedMessageContent = sanitize(data.format());
                sentMessage.editMessage(editedMessageContent).queue();
            }
        }, throwable -> {
            plugin.getSLF4JLogger().error("Failed to send message to Discord", throwable);
        });
    }

    public void sendToDiscord(PrivateMessageData data, MessageChannel targetChannel, boolean vcMode) {
        String message = sanitize(vcMode ? data.getMessage() : data.format());

        // name null-handling
        if(data.getReceivedPlayerName() == null) {
            String receivePlayerName = plugin
                    .getPlayerUUIDMapContainer()
                    .getNameFromUUID(data.getReceivedPlayerUUID());
            if(receivePlayerName != null) {
                message = message.replace(data.getReceivedPlayerUUID().toString(), receivePlayerName);
            }
        }

        // send message
        final String finalMessage = message; // to prevent from lambda capture issue
        targetChannel.sendMessage(finalMessage).queue(sentMessage -> {
            if (vcMode) {
                String editedMessageContent = sanitize(data.format());
                sentMessage.editMessage(editedMessageContent).queue();
            }
        }, throwable -> {
            plugin.getSLF4JLogger().error("Failed to send message to Discord", throwable);
        });
    }

    private static String sanitize(String message) {
        message = message.replace("@", "\\@");
        message = ChatColor.translateAlternateColorCodes('&', message);
        message = ChatColor.stripColor(message);
        return message;
    }
}
