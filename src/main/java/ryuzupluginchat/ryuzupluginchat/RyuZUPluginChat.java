package ryuzupluginchat.ryuzupluginchat;

import com.github.ucchyocean.lc3.LunaChat;
import com.github.ucchyocean.lc3.LunaChatAPI;
import com.github.ucchyocean.lc3.LunaChatConfig;
import com.github.ucchyocean.lc3.channel.Channel;
import com.github.ucchyocean.lc3.japanize.JapanizeType;
import com.github.ucchyocean.lc3.member.ChannelMemberBukkit;
import com.github.ucchyocean.lc3.util.Utility;
import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class RyuZUPluginChat extends JavaPlugin implements PluginMessageListener, Listener {
    public static LunaChatAPI lunachatapi;
    private static HashMap<String , List<String>> players = new HashMap<>();
    private static HashMap<String , String> prefix = new HashMap<>();
    private static HashMap<String , String> suffix = new HashMap<>();
    public static HashMap<String , String> reply = new HashMap<>();
    public static RyuZUPluginChat ryuzupluginchat;

    @Override
    public void onEnable() {
        ryuzupluginchat = this;
        if ( getServer().getPluginManager().isPluginEnabled("LunaChat") ) {
            lunachatapi = LunaChat.getAPI();
        }
        getServer().getPluginManager().registerEvents(this ,this);
        getServer().getMessenger().registerOutgoingPluginChannel(this, "ryuzuchat:ryuzuchat");
        getServer().getMessenger().registerIncomingPluginChannel(this, "ryuzuchat:ryuzuchat", this);
        Command command = new Command();
        Tell tell = new Tell();
        Reply reply = new Reply();
        Objects.requireNonNull(getCommand("rpc")).setExecutor(command);
        Objects.requireNonNull(getCommand("rpc")).setTabCompleter(command);
        Objects.requireNonNull(getCommand("tell")).setExecutor(tell);
        Objects.requireNonNull(getCommand("tell")).setTabCompleter(tell);
        Objects.requireNonNull(getCommand("t")).setExecutor(tell);
        Objects.requireNonNull(getCommand("t")).setTabCompleter(tell);
        Objects.requireNonNull(getCommand("reply")).setExecutor(reply);
        Objects.requireNonNull(getCommand("r")).setExecutor(reply);
        getServer().getScheduler().runTaskTimerAsynchronously(ryuzupluginchat, new CheckPlayers(), 0 , 20 * 10);
        getLogger().info(ChatColor.GREEN + "RyuZUPluginChatが起動しました");
    }

    @Override
    public void onPluginMessageReceived(String channel, @NotNull Player player, @NotNull byte[] message) {
        if (channel.equals("ryuzuchat:ryuzuchat")) {
            runAsyncTask(() -> {
                ByteArrayDataInput in = ByteStreams.newDataInput(message);
                String data = in.readUTF();
                Map<String, String> map = (Map<String, String>) jsonToMap(data);
                String playername = map.get("PlayerName");
                switch (map.get("System")) {
                    case "Chat":
                        Channel lunachannel = lunachatapi.getChannel(map.get("ChannelName"));
                        String msg = map.getOrDefault("Format" , "");
                        msg = msg.replace("[LuckPermsPrefix]", map.getOrDefault("LuckPermsPrefix", ""))
                                .replace("[LunaChatPrefix]", map.getOrDefault("LunaChatPrefix", ""))
                                .replace("[RyuZUMapPrefix]", map.getOrDefault("RyuZUMapPrefix", ""))
                                .replace("[SendServerName]", map.getOrDefault("SendServerName", ""))
                                .replace("[ReceiveServerName]", map.getOrDefault("ReceiveServerName", ""))
                                .replace("[PlayerName]", (map.getOrDefault("PlayerDisplayName", map.getOrDefault("PlayerName", ""))))
                                .replace("[RyuZUMapSuffix]", map.getOrDefault("RyuZUMapSuffix", ""))
                                .replace("[LunaChatSuffix]", map.getOrDefault("LunaChatSuffix", ""))
                                .replace("[LuckPermsSuffix]", map.getOrDefault("LuckPermsSuffix", ""));
                        msg = setColor(msg);
                        msg = msg.replace("[PreReplaceMessage]", (Boolean.parseBoolean(map.get("CanJapanese")) ? "(" + map.get("PreReplaceMessage") + ")" : ""))
                                .replace("[Message]", map.getOrDefault("Message", ""));
                        if (map.containsKey("ReceivePlayerName")) {
                            Player rp = getServer().getPlayer(map.get("ReceivePlayerName"));
                            if (map.containsKey("TellFormat") && !map.get("TellFormat").equals("")) {
                                msg = map.get("TellFormat");
                                msg = msg.replace("[LuckPermsPrefix]", map.getOrDefault("LuckPermsPrefix", ""))
                                        .replace("[LunaChatPrefix]", map.getOrDefault("LunaChatPrefix", ""))
                                        .replace("[RyuZUMapPrefix]", map.getOrDefault("RyuZUMapPrefix", ""))
                                        .replace("[SendServerName]", map.getOrDefault("SendServerName", ""))
                                        .replace("[ReceiveServerName]", map.getOrDefault("ReceiveServerName", ""))
                                        .replace("[PlayerName]", (map.getOrDefault("PlayerDisplayName", map.getOrDefault("PlayerName", ""))))
                                        .replace("[ReceivePlayerName]", map.getOrDefault("ReceivePlayerName", ""))
                                        .replace("[RyuZUMapSuffix]", map.getOrDefault("RyuZUMapSuffix", ""))
                                        .replace("[LunaChatSuffix]", map.getOrDefault("LunaChatSuffix", ""))
                                        .replace("[LuckPermsSuffix]", map.getOrDefault("LuckPermsSuffix", ""));
                                msg = setColor(msg);
                                msg = msg.replace("[PreReplaceMessage]", (Boolean.parseBoolean(map.get("CanJapanese")) ? "(" + map.get("PreReplaceMessage") + ")" : ""))
                                        .replace("[Message]", map.getOrDefault("Message", ""));
                                for (Player op : getServer().getOnlinePlayers().stream()
                                        .filter(p -> p.hasPermission("rpc.op"))
                                        .filter(p -> !p.equals(rp))
                                        .filter(p -> !playername.equals((p.getName())))
                                        .collect(Collectors.toList())) {
                                    op.sendMessage(msg);
                                    if (!map.containsKey("TellFormat") || map.get("TellFormat").equals("")) {
                                        op.sendMessage(ChatColor.RED + "---> " + map.get("ReceivePlayerName"));
                                    }
                                }
                            } else {
                                msg = ChatColor.YELLOW + "[Private]" + msg;
                                for (Player op : getServer().getOnlinePlayers().stream()
                                        .filter(p -> p.hasPermission("rpc.op"))
                                        .filter(p -> !p.equals(rp))
                                        .filter(p -> !playername.equals((p.getName())))
                                        .collect(Collectors.toList())) {
                                    op.sendMessage(msg);
                                    if (!map.containsKey("TellFormat") || map.get("TellFormat").equals("")) {
                                        op.sendMessage(ChatColor.RED + "---> " + map.get("ReceivePlayerName"));
                                    }
                                }
                                if (rp == null) { return; }
                            }
                            if (rp == null) { return; }
                            rp.sendMessage(msg);
                            if(!map.containsKey("TellFormat") || map.get("TellFormat").equals("")) { rp.sendMessage(ChatColor.RED + "---> " + map.get("ReceivePlayerName")); }
                            reply.put(map.get("ReceivePlayerName"), playername);
                            reply.put(playername, map.get("ReceivePlayerName"));
                            if (map.get("ReceiveServerName").equals(map.get("SendServerName"))) {
                                getLogger().info(msg);
                                if (!map.containsKey("TellFormat") || map.get("TellFormat").equals("")) {
                                    getLogger().info(ChatColor.RED + "---> " + map.get("ReceivePlayerName"));
                                }
                            } else {
                                getLogger().info("(" + ChatColor.RED + map.get("SendServerName") + ChatColor.WHITE + ")" + playername + " --> " + msg);
                                if (!map.containsKey("TellFormat") || map.get("TellFormat").equals("")) {
                                    getLogger().info(ChatColor.RED + "---> " + map.get("ReceivePlayerName"));
                                }
                            }
                            sendReturnPrivateMessage(playername, map);
                        } else if (map.containsKey("ReceivedPlayerName")) {
                            Player p = getServer().getPlayer(playername);
                            if (p == null) { return; }
                            if (map.containsKey("TellFormat") && !map.get("TellFormat").equals("")) {
                                msg = map.get("TellFormat");
                                msg = msg.replace("[LuckPermsPrefix]", map.getOrDefault("LuckPermsPrefix", ""))
                                        .replace("[LunaChatPrefix]", map.getOrDefault("LunaChatPrefix", ""))
                                        .replace("[RyuZUMapPrefix]", map.getOrDefault("RyuZUMapPrefix", ""))
                                        .replace("[SendServerName]", map.getOrDefault("SendServerName", ""))
                                        .replace("[ReceiveServerName]", map.getOrDefault("ReceiveServerName", ""))
                                        .replace("[PlayerName]", (map.getOrDefault("PlayerDisplayName", map.getOrDefault("PlayerName", ""))))
                                        .replace("[ReceivedPlayerName]", map.getOrDefault("ReceivedPlayerName", ""))
                                        .replace("[RyuZUMapSuffix]", map.getOrDefault("RyuZUMapSuffix", ""))
                                        .replace("[LunaChatSuffix]", map.getOrDefault("LunaChatSuffix", ""))
                                        .replace("[LuckPermsSuffix]", map.getOrDefault("LuckPermsSuffix", ""));
                                msg = setColor(msg);
                                msg = msg.replace("[PreReplaceMessage]", (Boolean.parseBoolean(map.get("CanJapanese")) ? "(" + map.get("PreReplaceMessage") + ")" : ""))
                                        .replace("[Message]", map.getOrDefault("Message", ""));
                                p.sendMessage(msg);
                            } else {
                                msg = ChatColor.YELLOW + "[Private]" + msg;
                                p.sendMessage(msg);
                                p.sendMessage(ChatColor.RED + "---> " + map.get("ReceivedPlayerName"));
                            }
                            reply.put(playername, map.get("ReceivedPlayerName"));
                            reply.put(map.get("ReceivedPlayerName"), playername);
                        } else if (map.containsKey("ChannelName") && !map.containsKey("Discord")) {
                            String channelname = map.get("ChannelName");
                            boolean ExistsChannel = lunachatapi.getChannel(channelname) != null;
                            if (!ExistsChannel) { return; }
                            String channelformat;
                            if (!map.containsKey("ChannelFormat") || map.get("ChannelFormat").equals("")) {
                                channelformat = map.get("LunaChannelFormat");
                                channelformat = channelformat.replace("%prefix", (map.getOrDefault("LuckPermsPrefix", "") +
                                        map.getOrDefault("RyuZUMapPrefix", "") +
                                        map.getOrDefault("LunaChatPrefix", "")))
                                        .replace("%suffix", (map.getOrDefault("LuckPermsSuffix", "") +
                                                map.getOrDefault("RyuZUMapSuffix", "") +
                                                map.getOrDefault("LunaChatSuffix", "")))
                                        .replace("%username", (map.getOrDefault("PlayerDisplayName", map.getOrDefault("PlayerName", ""))))
                                        .replace("%displayname", (map.getOrDefault("PlayerDisplayName", map.getOrDefault("PlayerName", ""))))
                                        .replace("%ch", channelname)
                                        .replace("%color", map.get("ChannelColorCode"));
                                channelformat = setColor(channelformat);
                                channelformat = channelformat.replace("%msg", map.get("Message"))
                                        .replace("%premsg", map.get("PreReplaceMessage"));
                                msg = channelformat;
                            } else {
                                channelformat = map.get("ChannelFormat")
                                        .replace("[ChannelName]", map.getOrDefault("ChannelName", ""))
                                        .replace("[ChannelAliasChannelAlias]", map.getOrDefault("ChannelAlias", ""))
                                        .replace("[ChannelColorCode]", map.getOrDefault("ChannelColorCode", ""));
                                msg = setColor(channelformat) + msg;
                            }
                            ChannelMemberBukkit member = ChannelMemberBukkit.getChannelMemberBukkit(playername);
                            for (Player p : getServer().getOnlinePlayers().stream()
                                    .filter(p -> lunachannel.getMembers().stream().map(m -> ((ChannelMemberBukkit) m).getPlayer()).collect(Collectors.toList()).contains(p) || p.hasPermission("rpc.op"))
                                    .filter(p -> !lunachatapi.getHidelist(ChannelMemberBukkit.getChannelMember((p.getName()))).contains(member))
                                    .collect(Collectors.toList())) {
                                p.sendMessage(msg);
                            }
                            if (map.get("ReceiveServerName").equals(map.get("SendServerName"))) {
                                getLogger().info(msg);
                            } else {
                                getLogger().info("(" + ChatColor.RED + map.get("SendServerName") + ChatColor.WHITE + ")" + playername + " --> " + map.get("Message") + ChatColor.BLUE + channelname);
                            }
                            addChannelLog(map.get("Message"), playername, channelname);
                        } else if(map.containsKey("Discord")) {
                            if(map.containsKey("ChannelName")) {
                                String channelname = map.get("ChannelName");
                                boolean ExistsChannel = lunachatapi.getChannel(channelname) != null;
                                if (!ExistsChannel) {return;}
                                String channelformat = lunachannel.getFormat();
                                channelformat = channelformat.replace("%prefix", (map.getOrDefault("LuckPermsPrefix", "") +
                                        map.getOrDefault("RyuZUMapPrefix", "") +
                                        map.getOrDefault("LunaChatPrefix", "")))
                                        .replace("%suffix", (map.getOrDefault("LuckPermsSuffix", "") +
                                                map.getOrDefault("RyuZUMapSuffix", "") +
                                                map.getOrDefault("LunaChatSuffix", "")))
                                        .replace("%username", (map.getOrDefault("Discord", "")))
                                        .replace("%displayname", (map.getOrDefault("Discord", "")))
                                        .replace("%ch", channelname)
                                        .replace("%color", "");
                                channelformat = ChatColor.WHITE + "[" + ChatColor.BLUE + "Discord" + ChatColor.WHITE + "]" + channelformat;
                                channelformat = setColor(channelformat);
                                channelformat = channelformat.replace("%msg", map.get("Message"))
                                        .replace("%premsg", "");
                                msg = channelformat;
                                ChannelMemberBukkit member = ChannelMemberBukkit.getChannelMemberBukkit(playername);
                                for (Player p : getServer().getOnlinePlayers().stream()
                                        .filter(p -> lunachannel.getMembers().stream().map(m -> ((ChannelMemberBukkit) m).getPlayer()).collect(Collectors.toList()).contains(p) || p.hasPermission("rpc.op"))
                                        .filter(p -> !lunachatapi.getHidelist(ChannelMemberBukkit.getChannelMember((p.getName()))).contains(member))
                                        .collect(Collectors.toList())) {
                                    p.sendMessage(msg);
                                }
                                getLogger().info(msg);
                            } else {
                                msg = ChatColor.WHITE + "[" + ChatColor.BLUE + "Discord" + ChatColor.WHITE + "]" + ChatColor.GREEN + map.get("Discord") + ChatColor.WHITE + " " + map.get("Message");
                                for (Player p : getServer().getOnlinePlayers()) { p.sendMessage(msg); }
                                getLogger().info(msg);
                            }
                        } else {
                            for (Player p : getServer().getOnlinePlayers()) { p.sendMessage(msg); }
                            if (map.get("ReceiveServerName").equals(map.get("SendServerName"))) {
                                getLogger().info(msg);
                            } else {
                                getLogger().info("(" + ChatColor.RED + map.get("SendServerName") + ChatColor.WHITE + ")" + playername + " --> " + map.get("Message") + ChatColor.BLUE);
                            }
                        }
                        break;
                    case "Prefix":
                        prefix.put(playername, map.get("Prefix"));
                        break;
                    case "Suffix":
                        suffix.put(playername, map.get("Suffix"));
                        break;
                    case "SystemMessage":
                        if (map.containsKey("SystemMessage")) {
                            String smsg = map.get("SystemMessage");
                            smsg = setColor(smsg
                                    .replace("[SendServerName]", map.getOrDefault("SendServerName", ""))
                                    .replace("[ReceiveServerName]", map.getOrDefault("ReceiveServerName", "")));
                            for (Player p : getServer().getOnlinePlayers()) {
                                p.sendMessage(smsg);
                            }
                        } else if (map.containsKey("Players")) {
                            List<String> list = new ArrayList<>(Arrays.asList(map.get("Players").split(",")));
                            players.put(map.get("ReceiveServerName"), list);
                        }
                        break;
                }
            });
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onChat(AsyncPlayerChatEvent e) {
        if(!e.isCancelled()) {
            Player p = e.getPlayer();
            boolean global = lunachatapi.getDefaultChannel(p.getName()) == null;
            if(global || e.getMessage().charAt(0) == '!') {
                sendGlobalMessage(p , e.getMessage().charAt(0) == '!' ? e.getMessage().substring(1) : e.getMessage());
            } else {
                sendChannelMessage(p , e.getMessage() , lunachatapi.getDefaultChannel(p.getName()));
            }
            e.setFormat("");
            e.setCancelled(true);
        }
    }

    public static void sendChannelMessage(Player p, String message, Channel channel) {
        runAsyncTask(() -> {
            Map<String , String> map = new HashMap<>();
            map.put("Message" , replaceMessage(message , p).replace("$" , "").replace("#" , ""));
            map.put("ChannelName" , channel.getName());
            map.put("LunaChannelFormat" , channel.getFormat());
            map.put("ChannelColorCode" , channel.getColorCode());
            map.put("ChannelAlias" , channel.getAlias());
            map.put("PreReplaceMessage" , message);
            map.put("CanJapanese" , String.valueOf(canJapanese(message , p)));
            map.put("LuckPermsPrefix" , getPrefix(p));
            map.put("LuckPermsSuffix" , getSuffix(p));
            map.put("RyuZUMapPrefix" , prefix.get(p.getName()));
            map.put("RyuZUMapSuffix" , suffix.get(p.getName()));
            map.put("PlayerName" , p.getName());
            map.put("PlayerDisplayName" , p.getDisplayName());
            map.put("System" , "Chat");
            sendPluginMessage("ryuzuchat:ryuzuchat" , mapToJson(map));
        });
    }

    public static void sendGlobalMessage(Player p, String message) {
        runAsyncTask(() -> {
            Map<String , String> map = new HashMap<>();
            map.put("Message" , replaceMessage(message , p).replace("$" , "").replace("#" , ""));
            map.put("PreReplaceMessage" , message);
            map.put("CanJapanese" , String.valueOf(canJapanese(message , p)));
            map.put("LuckPermsPrefix" , getPrefix(p));
            map.put("LuckPermsSuffix" , getSuffix(p));
            map.put("RyuZUMapPrefix" , prefix.get(p.getName()));
            map.put("RyuZUMapSuffix" , suffix.get(p.getName()));
            map.put("PlayerName" , p.getName());
            map.put("PlayerDisplayName" , p.getDisplayName());
            map.put("System" , "Chat");
            sendPluginMessage("ryuzuchat:ryuzuchat" , mapToJson(map));
        });
    }

    public void sendPrivateMessage(Player p , String message , String receiver) {
        runAsyncTask(() -> {
            ChannelMemberBukkit cp =  ChannelMemberBukkit.getChannelMemberBukkit(p.getName());
            Map<String , String> map = new HashMap<>();
            map.put("Message" , replaceMessage(message , p).replace("$" , "").replace("#" , ""));
            map.put("PreReplaceMessage" , message);
            map.put("CanJapanese" , String.valueOf(canJapanese(message , p)));
            map.put("ReceivePlayerName" , receiver);
            map.put("LuckPermsPrefix" , getPrefix(p));
            map.put("LuckPermsSuffix" , getSuffix(p));
            map.put("RyuZUMapPrefix" , prefix.get(p.getName()));
            map.put("RyuZUMapSuffix" , suffix.get(p.getName()));
            map.put("PlayerName" , p.getName());
            map.put("PlayerDisplayName" , p.getDisplayName());
            map.put("System" , "Chat");
            sendPluginMessage("ryuzuchat:ryuzuchat" , mapToJson(map));
        });
    }

    public void sendSystemMessage(String message) {
        runAsyncTask(() -> {
            Map<String , String> map = new HashMap<>();
            map.put("SystemMessage" , setColor(message));
            map.put("System" , "SystemMessage");
            sendPluginMessage("ryuzuchat:ryuzuchat" , mapToJson(map));
        });
    }

    public void sendSystemMessage(String message , Player p) {
        runAsyncTask(() -> {
            Map<String , String> map = new HashMap<>();
            String msg = message;
            msg = msg.replace("[LuckPermsPrefix]", replaceNull(getPrefix(p)))
                    .replace("[RyuZUMapPrefix]", replaceNull(prefix.get(p.getName())))
                    .replace("[RyuZUMapSuffix]", replaceNull(suffix.get(p.getName())))
                    .replace("[LuckPermsSuffix]", replaceNull(getSuffix(p)));
            map.put("SystemMessage" , setColor(msg));
            map.put("LuckPermsPrefix" , getPrefix(p));
            map.put("LuckPermsSuffix" , getSuffix(p));
            map.put("RyuZUMapPrefix" , prefix.get(p.getName()));
            map.put("RyuZUMapSuffix" , suffix.get(p.getName()));
            map.put("System" , "SystemMessage");
            sendPluginMessage("ryuzuchat:ryuzuchat" , mapToJson(map));
        });
    }

    public void sendReturnPrivateMessage(String p , Map<String , String> data) {
        runAsyncTask(() -> {
            Player rp = Bukkit.getPlayer(data.get("ReceivePlayerName"));
            Map<String , String> map = new HashMap<>(data);
            map.put("ReceivedPlayerName" , map.get("ReceivePlayerName"));
            map.remove("ReceivePlayerName");
            map.put("ReceivedPlayerLuckPermsPrefix" , getPrefix(rp));
            map.put("ReceivedPlayerLuckPermsSuffix" , getSuffix(rp));
            map.put("ReceivedPlayerRyuZUMapPrefix" , prefix.get(rp.getName()));
            map.put("ReceivedPlayerRyuZUMapSuffix" , suffix.get(rp.getName()));
            map.put("ReceivedPlayerPlayerName" , rp.getName());
            map.put("ReceivedPlayerPlayerDisplayName" , rp.getDisplayName());
            map.put("System" , "Chat");
            sendPluginMessage("ryuzuchat:ryuzuchat" , mapToJson(map));
        });
    }

    private static String replaceMessage(String msg, Player p) {
        String message = msg;
        LunaChatConfig config = LunaChat.getConfig();
        if(canJapanese(msg , p)) {message = lunachatapi.japanize(message , config.getJapanizeType()); }
        if(config.isEnableNormalChatColorCode() || p.hasPermission("lunachat.allowcc")) {message = setColor(message); }
        return message;
    }

    private String replaceNull(String s) {
        return (s == null ? "" : s);
    }

    private static boolean canJapanese(String msg, Player p) {
        LunaChatConfig config = LunaChat.getConfig();
        return lunachatapi.isPlayerJapanize(p.getName()) &&
                config.getJapanizeType() != JapanizeType.NONE &&
                msg.getBytes(StandardCharsets.UTF_8).length <= msg.length() &&
                msg.charAt(0) != '#' &&
                msg.charAt(0) != '$';
    }

    private static String setColor(String msg) {
        String replaced = msg;
        replaced = replaceToHexFromRGB(replaced);
        return ChatColor.translateAlternateColorCodes('&' , replaced);
    }

    private static void sendPluginMessage(String channel, String data) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(data);
        Player player = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
        if (player != null) {
            player.sendPluginMessage(ryuzupluginchat, channel, out.toByteArray());
        }
    }

    private static String getPrefix(Player player) {
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

    private static String getSuffix(Player player) {
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

    private static String mapToJson(Map<String, String> map) {
        Gson gson = new Gson();
        return gson.toJson(map);
    }

    private Map<String,?> jsonToMap(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, Map.class);
    }

    private static String replaceToHexFromRGB(String text) {
        String regex = "\\{.+?}";
        List<String> RGBcolors = new ArrayList<>();
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            RGBcolors.add(matcher.group());
        }
        for(String hexcolor : RGBcolors) {
            String[] rgbs = hexcolor.replace("{color:" , "").replace("}" , "").split(",");
            for(int i = 0 ; i < 3 ; i++) {
                try {
                    if(Integer.parseInt(rgbs[i]) < 0 || Integer.parseInt(rgbs[i]) > 255) {
                        return text;
                    }
                } catch (NumberFormatException e) {
                    return text;
                }
            }
            Color rgb = new Color(Integer.parseInt(rgbs[0]),Integer.parseInt(rgbs[1]),Integer.parseInt(rgbs[2]));
            String hex = "#"+Integer.toHexString(rgb.getRGB()).substring(2);
            text = text.replace(hexcolor , ChatColor.of(hex) + "");
        }
        return text;
    }

    public void setFormat(String GroupName , String Format) {
        Map<String , String> map = new HashMap<>();
        map.put("Arg0" , GroupName);
        map.put("Arg1" , Format);
        map.put("EditTarget" , "Format");
        map.put("EditType" , "set");
        map.put("System" , "EditConfig");
        sendPluginMessage("ryuzuchat:ryuzuchat" , mapToJson(map));
    }

    public void setChannelFormat(String GroupName , String Format) {
        Map<String , String> map = new HashMap<>();
        map.put("Arg0" , GroupName);
        map.put("Arg1" , Format);
        map.put("EditTarget" , "ChannelFormat");
        map.put("EditType" , "set");
        map.put("System" , "EditConfig");
        sendPluginMessage("ryuzuchat:ryuzuchat" , mapToJson(map));
    }

    public void setTellFormat(String GroupName , String Format) {
        Map<String , String> map = new HashMap<>();
        map.put("Arg0" , GroupName);
        map.put("Arg1" , Format);
        map.put("EditTarget" , "TellFormat");
        map.put("EditType" , "set");
        map.put("System" , "EditConfig");
        sendPluginMessage("ryuzuchat:ryuzuchat" , mapToJson(map));
    }

    public void addServer(String GroupName , String ServerName) {
        Map<String , String> map = new HashMap<>();
        map.put("Arg0" , GroupName);
        map.put("Arg1" , ServerName);
        map.put("EditTarget" , "List");
        map.put("EditType" , "add");
        map.put("System" , "EditConfig");
        sendPluginMessage("ryuzuchat:ryuzuchat" , mapToJson(map));
    }

    public void removeServer(String GroupName , String ServerName) {
        Map<String , String> map = new HashMap<>();
        map.put("Arg0" , GroupName);
        map.put("Arg1" , ServerName);
        map.put("EditTarget" , "List");
        map.put("EditType" , "remove");
        map.put("System" , "EditConfig");
        sendPluginMessage("ryuzuchat:ryuzuchat" , mapToJson(map));
    }

    public void removeGroup(String GroupName) {
        Map<String , String> map = new HashMap<>();
        map.put("Arg0" , GroupName);
        map.put("EditTarget" , "Group");
        map.put("EditType" , "remove");
        map.put("System" , "EditConfig");
        sendPluginMessage("ryuzuchat:ryuzuchat" , mapToJson(map));
    }

    public void setPrefix(String p , String data) {
        Map<String , String> map = new HashMap<>();
        prefix.put(p , setColor(data));
        map.put("PlayerName" , p);
        map.put("Prefix" , setColor(data));
        map.put("System" , "Prefix");
        sendPluginMessage("ryuzuchat:ryuzuchat" , mapToJson(map));
    }

    public void setSuffix(String p , String data) {
        Map<String , String> map = new HashMap<>();
        suffix.put(p , setColor(data));
        map.put("PlayerName" , p);
        map.put("Suffix" , setColor(data));
        map.put("System" , "Suffix");
        sendPluginMessage("ryuzuchat:ryuzuchat" , mapToJson(map));
    }

    public static List<String> getPlayers() {
        List<String> list = new ArrayList<>();
        players.values().forEach(list::addAll);
        return list;
    }

    public void sendPlayers() {
        Map<String , String> map = new HashMap<>();
        String list = StringUtils.join(getServer().getOnlinePlayers().stream().map(HumanEntity::getName).collect(Collectors.toList()) , ",");
        map.put("System" , "SystemMessage");
        map.put("Players" , list);
        sendPluginMessage("ryuzuchat:ryuzuchat" , mapToJson(map));
    }

    /**
     * 指定された日付のログファイル名を生成して返します。
     * @param date 日付
     * @return ログファイル名
     */
    private String getFolderPath(Date date) {
        return LunaChat.getDataFolder() +
                File.separator + "logs" +
                File.separator + new SimpleDateFormat("yyyy-MM-dd").format(date);
    }

    /**
     * 指定された日付のログファイルを取得します。
     * 取得できない場合は、nullを返します。
     * @param date 日付
     * @return 指定された日付のログファイル
     */
    private File getLogFile(String date , String channelname) {
        File dir = new File(getFolderPath(new Date()));
        if ( !dir.exists() || !dir.isDirectory() ) { dir.mkdirs(); }
        File file = new File(dir, channelname + ".log");

        if ( date == null ) { return file; }

        Date d;
        try {
            if ( date.matches("[0-9]{4}") ) {
                date = Calendar.getInstance().get(Calendar.YEAR) + date;
            }
            if ( date.matches("[0-9]{8}") ) {
                d = new SimpleDateFormat("yyyyMMdd").parse(date);
            } else {
                return null;
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }

        File folder = new File(getFolderPath(d));
        if ( !folder.exists() || !folder.isDirectory() ) {
            return null;
        }

        File f = new File(folder, channelname + ".log");
        if ( !f.exists() ) {
            return null;
        }

        return f;
    }

    /**
     * ログを出力する
     * @param message ログ内容
     * @param player 発言者名
     */
    public synchronized void addChannelLog(final String message, final String player , final String channelname) {
        // 以降の処理を、発言処理の負荷軽減のため、非同期実行にする。(see issue #40.)
        LunaChat.runAsyncTask(() -> {

            String msg = Utility.stripColorCode(message);
            if ( msg == null ) msg = "";
            msg = msg.replace(",", "，");

            try (OutputStreamWriter writer = new OutputStreamWriter(
                    new FileOutputStream(Objects.requireNonNull(getLogFile(null, channelname)), true), StandardCharsets.UTF_8); ) {

                String str = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "," + msg + "," + player;
                writer.write(str + "\r\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public static class CheckPlayers implements Runnable {
        @Override
        public void run() {
            RyuZUPluginChat.ryuzupluginchat.sendPlayers();
        }
    }

    public static void runAsyncTask(Runnable task) {
        Bukkit.getScheduler().runTaskAsynchronously(ryuzupluginchat , task);
    }
}
