package ryuzupluginchat.ryuzupluginchat;

import com.github.ucchyocean.lc.channel.ChannelPlayer;
import com.github.ucchyocean.lc.event.LunaChatChannelChatEvent;
import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public final class RyuZUPluginChat extends JavaPlugin implements PluginMessageListener, Listener {

    @Override
    public void onEnable() {
        // Plugin startup logic
        getServer().getMessenger().registerIncomingPluginChannel(this, "ryuzuchat:ryuzuchat", this);
        getLogger().info("Plugin版リューズは天才が起動したぞ!");
    }

    @Override
    public void onPluginMessageReceived(String channel, @NotNull Player player, @NotNull byte[] message) {
        if (channel.equals("ryuzuchat:ryuzuchat")) {
            ByteArrayDataInput in = ByteStreams.newDataInput(message);
            String data = in.readUTF();
            Map<String , String> map = (Map<String, String>) jsonToMap(data);
            for(Player p : getServer().getOnlinePlayers()) {
                p.sendMessage(map.get("LuckPermsPrefix") +
                        map.get("LunaChatPrefix") +
                        map.get("PlayerName") +
                        map.get("LunaChatSuffix") +
                        map.get("LuckPermsSuffix") +
                        ChatColor.BLUE + ">>" + ChatColor.WHITE +
                        map.get("Message"));
            }
        }
    }

    @EventHandler
    public void onChat(LunaChatChannelChatEvent e) {
        System.out.println("でばめS");
        Map<String , String> map = new HashMap<>();
        ChannelPlayer cp = e.getPlayer();
        Player p = e.getPlayer().getPlayer();
        map.put("ServerName" , getServer().getName());
        map.put("Message" , e.getMessageFormat());
        map.put("ChannelName" , e.getChannelName());
        map.put("LunaChatPrefix" , cp.getPrefix());
        map.put("LunaChatSuffix" , cp.getSuffix());
        map.put("LuckPermsPrefix" , getPrefix(p));
        map.put("LuckPermsSuffix" , getSuffix(p));
        map.put("PlayerName" , p.getName());
        sendPluginMessage("ryuzuchat:ryuzuchat" , mapToJson(map));
    }

    private void sendPluginMessage(String channel, String data) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(data);
        Player player = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
        if (player != null) {
            player.sendPluginMessage(this, channel, out.toByteArray());
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
