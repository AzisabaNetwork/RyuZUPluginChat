package ryuzupluginchat.ryuzupluginchat;

import com.github.ucchyocean.lc.channel.ChannelPlayer;
import com.github.ucchyocean.lc.event.LunaChatChannelChatEvent;
import com.github.ucchyocean.lc3.LunaChat;
import com.github.ucchyocean.lc3.LunaChatAPI;
import com.github.ucchyocean.lc3.LunaChatConfig;
import com.github.ucchyocean.lc3.bukkit.event.LunaChatBukkitChannelChatEvent;
import com.github.ucchyocean.lc3.bukkit.event.LunaChatBukkitChannelMessageEvent;
import com.github.ucchyocean.lc3.channel.Channel;
import com.github.ucchyocean.lc3.japanize.JapanizeType;
import com.github.ucchyocean.lc3.member.ChannelMember;
import com.github.ucchyocean.lc3.member.ChannelMemberBukkit;
import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public final class RyuZUPluginChat extends JavaPlugin implements PluginMessageListener, Listener {
    public static LunaChatAPI lunachatapi;

    @Override
    public void onEnable() {
        // Plugin startup logic
        if ( getServer().getPluginManager().isPluginEnabled("LunaChat") ) {
            lunachatapi = LunaChat.getAPI();
        }
        getServer().getPluginManager().registerEvents(this ,this);
        getServer().getMessenger().registerOutgoingPluginChannel(this, "ryuzuchat:ryuzuchat");
        getServer().getMessenger().registerIncomingPluginChannel(this, "ryuzuchat:ryuzuchat", this);
        getLogger().info(ChatColor.GREEN + "RyuZUPluginChatが起動しました");
    }

    @Override
    public void onPluginMessageReceived(String channel, @NotNull Player player, @NotNull byte[] message) {
        if (channel.equals("ryuzuchat:ryuzuchat")) {
            ByteArrayDataInput in = ByteStreams.newDataInput(message);
            String data = in.readUTF();
            Map<String , String> map = (Map<String, String>) jsonToMap(data);
            boolean ExistsChannel = map.get("ChannelName") != null && lunachatapi.getChannel(map.get("ChannelName")) != null;
            Channel lunachannel =  lunachatapi.getChannel(map.get("ChannelName"));
            String msg =  ChatColor.translateAlternateColorCodes('&' , map.get("Format"));
            msg = msg.replace("[LuckPermsPrefix]" , (map.get("LuckPermsPrefix") == null ? "" : map.get("LuckPermsPrefix")))
                    .replace("[LunaChatPrefix]" , (map.get("LunaChatPrefix") == null ? "" : map.get("LunaChatPrefix")))
                    .replace("[SendServerName]" , (map.get("SendServerName") == null ? "" : map.get("SendServerName")))
                    .replace("[ReceiveServerName]" , (map.get("ReceiveServerName") == null ? "" : map.get("ReceiveServerName")))
                    .replace("[ChannelName]" , (map.get("ChannelName") == null ? "" : map.get("ChannelName")))
                    .replace("[LunaChatChannelAlias]" , (ExistsChannel ? lunachannel.getAlias() : ""))
                    .replace("[PlayerName]" , (map.get("PlayerName") == null ? "" : map.get("PlayerName")))
                    .replace("[LunaChatSuffix]" , (map.get("LunaChatSuffix") == null ? "" : map.get("LunaChatSuffix")))
                    .replace("[LuckPermsSuffix]" , (map.get("LuckPermsSuffix") == null ? "" : map.get("LuckPermsSuffix")))
                    .replace("[Message]" , (map.get("Message") == null ? "" : map.get("Message")));
            for(Player p : (ExistsChannel ? lunachannel.getMembers().stream().map(m -> ((ChannelMemberBukkit) m).getPlayer()).collect(Collectors.toList()) : getServer().getOnlinePlayers())) {
                p.sendMessage(msg);
            }
            getLogger().info("(" + ChatColor.RED +  map.get("SendServerName") + ChatColor.WHITE + ")" + map.get("PlayerName") + " --> " + map.get("Message") + ChatColor.BLUE + (map.get("ChannelName") == null ? "" : map.get("ChannelName")));
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Map<String , String> map = new HashMap<>();
        Player p = e.getPlayer();
        Collection<Channel> channels = lunachatapi.getChannelsByPlayer(p.getName());
        if(channels != null && channels.size() != 0){ return; }
        ChannelMemberBukkit cp =  ChannelMemberBukkit.getChannelMemberBukkit(p.getName());
        map.put("Message" , replaceMessage(e.getMessage() , p));
        map.put("LuckPermsPrefix" , getPrefix(p));
        map.put("LuckPermsSuffix" , getSuffix(p));
        map.put("PlayerName" , p.getName());
        sendPluginMessage("ryuzuchat:ryuzuchat" , mapToJson(map));
    }

    @EventHandler
    public void onChat(LunaChatBukkitChannelMessageEvent e) {
        Map<String , String> map = new HashMap<>();
        ChannelMemberBukkit cp = (ChannelMemberBukkit) e.getMember();
        Player p = cp.getPlayer();
        map.put("Message" , replaceMessage(e.getMessage() , p));
        map.put("ChannelName" , e.getChannelName());
        map.put("LunaChatPrefix" , cp.getPrefix());
        map.put("LunaChatSuffix" , cp.getSuffix());
        map.put("LuckPermsPrefix" , getPrefix(p));
        map.put("LuckPermsSuffix" , getSuffix(p));
        map.put("PlayerName" , p.getName());
        sendPluginMessage("ryuzuchat:ryuzuchat" , mapToJson(map));
    }

    private String replaceMessage(String msg , Player p) {
        String message = msg;
        LunaChatConfig config = LunaChat.getConfig();
        ChannelMemberBukkit cp = ChannelMemberBukkit.getChannelMemberBukkit(p.getName());
        if(lunachatapi.isPlayerJapanize(p.getName()) && config.getJapanizeType() != JapanizeType.NONE) {message = lunachatapi.japanize(message , config.getJapanizeType()); }
        if(config.isEnableNormalChatColorCode() && p.hasPermission("lunachat.allowcc")) {message = setColor(message); }
        return message;
    }

    private String setColor(String msg) {
        return ChatColor.translateAlternateColorCodes('&' , msg);
    }

    private void sendPluginMessage(String channel, String data) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(data);
        Player player = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
        if (player != null) {
            player.sendPluginMessage(this, channel, out.toByteArray());
            getServer().sendPluginMessage(this, channel, out.toByteArray());
        }
    }

    private String getPrefix(Player player) {
        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            LuckPerms api = provider.getProvider();
            User user = api.getUserManager().getUser(player.getUniqueId());
            if (user != null) {
                return user.getCachedData().getMetaData().getPrefix();
            }
        }
        return null;
    }

    private String getSuffix(Player player) {
        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            LuckPerms api = provider.getProvider();
            User user = api.getUserManager().getUser(player.getUniqueId());
            if (user != null) {
                return user.getCachedData().getMetaData().getSuffix();
            }
        }
        return null;
    }

    private String mapToJson(Map<String, String> map) {
        Gson gson = new Gson();
        return gson.toJson(map);
    }

    private Map<String,?> jsonToMap(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, Map.class);
    }

}
