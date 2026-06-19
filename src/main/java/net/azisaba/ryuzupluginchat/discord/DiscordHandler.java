package net.azisaba.ryuzupluginchat.discord;

import lombok.RequiredArgsConstructor;
import net.azisaba.ryuzupluginchat.RyuZUPluginChat;
import net.azisaba.ryuzupluginchat.discord.data.ChannelChatSyncData;
import net.azisaba.ryuzupluginchat.discord.data.GlobalChatSyncData;
import net.azisaba.ryuzupluginchat.discord.data.PrivateChatSyncData;
import net.azisaba.ryuzupluginchat.discord.deliverer.DiscordMessageDeliverer;
import net.azisaba.ryuzupluginchat.discord.deliverer.ServerChatMessageDeliverer;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.bukkit.Bukkit;

import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class DiscordHandler extends ListenerAdapter {
    private JDA jda;
    private final RyuZUPluginChat plugin;
    private DiscordMessageDeliverer discordMessageDeliverer;
    private ServerChatMessageDeliverer serverChatMessageDeliverer;

    // どのチャンネルIDがどの処理（Global, Channel, Private）に紐付いているかを管理
    private final ConcurrentHashMap<Long, DiscordInputType> channelConfigurations = new ConcurrentHashMap<>();

    public boolean init(String token) {
        try {
            // JDAの構築
            jda = JDABuilder.createDefault(token)
                    .enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT)
                    .addEventListeners(this)
                    .build();

            // 接続完了まで待機
            jda.awaitReady();

            this.discordMessageDeliverer = new DiscordMessageDeliverer(plugin);
            this.serverChatMessageDeliverer = new ServerChatMessageDeliverer(plugin, jda);

            return true;
        } catch (Exception e) {
            plugin.getLogger().severe("Discordの初期化に失敗しました: " + e.getMessage());
            return false;
        }
    }

    // DiscordからMinecraftへの入力イベント
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        long channelId = event.getChannel().getIdLong();
        DiscordInputType type = channelConfigurations.get(channelId);

        if (type == null) return;

        switch (type) {
            case GLOBAL:
                discordMessageDeliverer.sendToGlobal(event);
                break;
            case CHANNEL:
                // syncDataが必要な場合は、別途Mapなどで管理して渡す
                discordMessageDeliverer.sendToChannel(event, type.getSyncData());
                break;
        }
    }

    public void connectUsing(DiscordMessageConnection connectionData) {
        long channelId = connectionData.getDiscordChannelId();

        // Global Chat Sync
        if (connectionData.getGlobalChatSyncData().isEnabled()) {
            GlobalChatSyncData data = connectionData.getGlobalChatSyncData();
            if (data.isDiscordInputEnabled()) {
                channelConfigurations.put(channelId, DiscordInputType.GLOBAL);
            }
            registerGlobalToDiscord(channelId, data.isVoiceChatMode());
        }

        // Channel Chat Sync
        if (connectionData.getChannelChatSyncData().isEnabled()) {
            ChannelChatSyncData data = connectionData.getChannelChatSyncData();
            if (data.isDiscordInputEnabled()) {
                // 必要に応じてsyncDataを保持するロジックを追加
                channelConfigurations.put(channelId, DiscordInputType.CHANNEL);
            }
            registerLunaChatChannelToDiscord(data, channelId, data.isVoiceChatMode());
        }

        // Private Chat Sync
        if (connectionData.getPrivateChatSyncData().isEnabled()) {
            PrivateChatSyncData data = connectionData.getPrivateChatSyncData();
            registerPrivateToDiscord(channelId, data.isVoiceChatMode());
        }
    }

    private void registerGlobalToDiscord(long chId, boolean vcMode) {
        MessageChannel targetChannel = jda.getTextChannelById(chId);
        if (targetChannel == null) return;

        plugin.getSubscriber().registerPublicConsumer((data) -> {
            if (data.isFromDiscord()) return;
            Bukkit.getScheduler().runTaskAsynchronously(plugin,
                    () -> serverChatMessageDeliverer.sendToDiscord(data, targetChannel, vcMode));
        });
    }

    private void registerLunaChatChannelToDiscord(ChannelChatSyncData syncData, long chId, boolean vcMode) {
        MessageChannel targetChannel = jda.getTextChannelById(chId);
        if (targetChannel == null) return;

        plugin.getSubscriber().registerChannelChatConsumer((data) -> {
            if (data.isFromDiscord()) return;
            if (!syncData.isMatch(data.getLunaChatChannelName())) return;

            Bukkit.getScheduler().runTaskAsynchronously(plugin,
                    () -> serverChatMessageDeliverer.sendToDiscord(data, targetChannel, vcMode));
        });
    }

    private void registerPrivateToDiscord(long chId, boolean vcMode) {
        MessageChannel targetChannel = jda.getTextChannelById(chId);
        if (targetChannel == null) return;

        plugin.getSubscriber().registerTellConsumer((data) ->
                Bukkit.getScheduler().runTaskAsynchronously(plugin,
                        () -> serverChatMessageDeliverer.sendToDiscord(data, targetChannel, vcMode)));
    }

    public void disconnect() {
        if (jda != null) jda.shutdown();
    }

    // 内部判別用
    private enum DiscordInputType {
        GLOBAL, CHANNEL;
        private ChannelChatSyncData syncData;
        public void setSyncData(ChannelChatSyncData data) { this.syncData = data; }
        public ChannelChatSyncData getSyncData() { return syncData; }
    }
}
