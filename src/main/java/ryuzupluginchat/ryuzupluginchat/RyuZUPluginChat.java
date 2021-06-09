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
import com.sun.org.apache.regexp.internal.RE;
import com.sun.org.apache.xpath.internal.operations.Bool;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class RyuZUPluginChat extends JavaPlugin implements PluginMessageListener, Listener {
    private static LunaChatAPI lunachatapi;
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
            ByteArrayDataInput in = ByteStreams.newDataInput(message);
            String data = in.readUTF();
            Map<String , String> map = (Map<String, String>) jsonToMap(data);
            switch (map.get("System")) {
                case "Chat":
                    boolean ExistsChannel = map.get("ChannelName") != null && lunachatapi.getChannel(map.get("ChannelName")) != null;
                    Channel lunachannel = lunachatapi.getChannel(map.get("ChannelName"));
                    String msg = setColor(map.get("Format"));
                    msg = msg.replace("[LuckPermsPrefix]", (map.get("LuckPermsPrefix") == null ? "" : map.get("LuckPermsPrefix")))
                            .replace("[LunaChatPrefix]", (map.get("LunaChatPrefix") == null ? "" : map.get("LunaChatPrefix")))
                            .replace("[RyuZUMapPrefix]", (map.get("RyuZUMapPrefix") == null ? "" : map.get("RyuZUMapPrefix")))
                            .replace("[SendServerName]", (map.get("SendServerName") == null ? "" : map.get("SendServerName")))
                            .replace("[ReceiveServerName]", (map.get("ReceiveServerName") == null ? "" : map.get("ReceiveServerName")))
                            .replace("[PlayerName]", (map.get("PlayerDisplayName") == null ? (map.get("PlayerName") == null ? "" : map.get("PlayerName")) : map.get("PlayerDisplayName")))
                            .replace("[RyuZUMapSuffix]", (map.get("RyuZUMapSuffix") == null ? "" : map.get("RyuZUMapSuffix")))
                            .replace("[LunaChatSuffix]", (map.get("LunaChatSuffix") == null ? "" : map.get("LunaChatSuffix")))
                            .replace("[LuckPermsSuffix]", (map.get("LuckPermsSuffix") == null ? "" : map.get("LuckPermsSuffix")))
                            .replace("[PreReplaceMessage]", (Boolean.parseBoolean(map.get("CanJapanese")) ? "(" + map.get("PreReplaceMessage") + ")" : ""))
                            .replace("[Message]", (map.get("Message") == null ? "" : map.get("Message")));
                    if (map.get("ReceivePlayerName") != null) {
                        Player rp = getServer().getPlayer(map.get("ReceivePlayerName"));
                        if (getServer().getPlayer(map.get("ReceivePlayerName")) == null) { return; }
                        if(map.get("TellFormat") != null) {
                            msg = setColor(map.get("TellFormat"));
                            msg = msg.replace("[LuckPermsPrefix]", (map.get("LuckPermsPrefix") == null ? "" : map.get("LuckPermsPrefix")))
                                    .replace("[LunaChatPrefix]", (map.get("LunaChatPrefix") == null ? "" : map.get("LunaChatPrefix")))
                                    .replace("[RyuZUMapPrefix]", (map.get("RyuZUMapPrefix") == null ? "" : map.get("RyuZUMapPrefix")))
                                    .replace("[SendServerName]", (map.get("SendServerName") == null ? "" : map.get("SendServerName")))
                                    .replace("[ReceiveServerName]", (map.get("ReceiveServerName") == null ? "" : map.get("ReceiveServerName")))
                                    .replace("[PlayerName]", (map.get("PlayerDisplayName") == null ? (map.get("PlayerName") == null ? "" : map.get("PlayerName")) : map.get("PlayerDisplayName")))
                                    .replace("[ReceivePlayerName]", (map.get("ReceivePlayerName") == null ? "" : map.get("ReceivePlayerName")))
                                    .replace("[RyuZUMapSuffix]", (map.get("RyuZUMapSuffix") == null ? "" : map.get("RyuZUMapSuffix")))
                                    .replace("[LunaChatSuffix]", (map.get("LunaChatSuffix") == null ? "" : map.get("LunaChatSuffix")))
                                    .replace("[LuckPermsSuffix]", (map.get("LuckPermsSuffix") == null ? "" : map.get("LuckPermsSuffix")))
                                    .replace("[PreReplaceMessage]", (Boolean.parseBoolean(map.get("CanJapanese")) ? "(" + map.get("PreReplaceMessage") + ")" : ""))
                                    .replace("[Message]", (map.get("Message") == null ? "" : map.get("Message")));
                            rp.sendMessage(msg);
                        } else {
                            msg = ChatColor.YELLOW + "[Private]" + msg;
                            rp.sendMessage(msg);
                            rp.sendMessage(ChatColor.RED + "--- > " + map.get("ReceivePlayerName"));
                        }
                        reply.put(map.get("ReceivePlayerName"), map.get("PlayerName"));
                        reply.put(map.get("PlayerName"), map.get("ReceivePlayerName"));
                        for (Player op : getServer().getOnlinePlayers().stream().filter(p -> p.hasPermission("rpc.op")).filter(p -> !p.equals(rp)).filter(p -> !map.get("PlayerName").equals((p.getName()))).collect(Collectors.toList())) {
                            op.sendMessage(msg);
                            if(map.get("TellFormat") == null) {
                                op.sendMessage(ChatColor.RED + "--- > " + map.get("ReceivePlayerName"));
                            }
                        }
                        if (map.get("ReceiveServerName").equals(map.get("SendServerName"))) {
                            getLogger().info(msg);
                            getLogger().info(ChatColor.RED + "--- > " + map.get("ReceivePlayerName"));
                        } else {
                            getLogger().info("(" + ChatColor.RED + map.get("SendServerName") + ChatColor.WHITE + ")" + map.get("PlayerName") + " --> " + map.get("Message") + ChatColor.BLUE + (map.get("ChannelName") == null ? "" : map.get("ChannelName")));
                            getLogger().info(ChatColor.RED + "--- > " + map.get("ReceivePlayerName"));
                        }
                        sendReturnPrivateMessage(map.get("PlayerName"), map);
                    } else if (map.get("ReceivedPlayerName") != null) {
                        Player p = getServer().getPlayer(map.get("PlayerName"));
                        if(map.get("TellFormat") != null) {
                            msg = setColor(map.get("TellFormat"));
                            msg = msg.replace("[LuckPermsPrefix]", (map.get("LuckPermsPrefix") == null ? "" : map.get("LuckPermsPrefix")))
                                    .replace("[LunaChatPrefix]", (map.get("LunaChatPrefix") == null ? "" : map.get("LunaChatPrefix")))
                                    .replace("[RyuZUMapPrefix]", (map.get("RyuZUMapPrefix") == null ? "" : map.get("RyuZUMapPrefix")))
                                    .replace("[SendServerName]", (map.get("SendServerName") == null ? "" : map.get("SendServerName")))
                                    .replace("[ReceiveServerName]", (map.get("ReceiveServerName") == null ? "" : map.get("ReceiveServerName")))
                                    .replace("[PlayerName]", (map.get("PlayerDisplayName") == null ? (map.get("PlayerName") == null ? "" : map.get("PlayerName")) : map.get("PlayerDisplayName")))
                                    .replace("[ReceivedPlayerName]", (map.get("ReceivedPlayerName") == null ? "" : map.get("ReceivedPlayerName")))
                                    .replace("[RyuZUMapSuffix]", (map.get("RyuZUMapSuffix") == null ? "" : map.get("RyuZUMapSuffix")))
                                    .replace("[LunaChatSuffix]", (map.get("LunaChatSuffix") == null ? "" : map.get("LunaChatSuffix")))
                                    .replace("[LuckPermsSuffix]", (map.get("LuckPermsSuffix") == null ? "" : map.get("LuckPermsSuffix")))
                                    .replace("[PreReplaceMessage]", (Boolean.parseBoolean(map.get("CanJapanese")) ? "(" + map.get("PreReplaceMessage") + ")" : ""))
                                    .replace("[Message]", (map.get("Message") == null ? "" : map.get("Message")));
                            p.sendMessage(msg);
                        } else {
                            msg = ChatColor.YELLOW + "[Private]" + msg;
                            p.sendMessage(msg);
                            p.sendMessage(ChatColor.RED + "--- > " + map.get("ReceivedPlayerName"));
                        }
                        reply.put(map.get("PlayerName"), map.get("ReceivedPlayerName"));
                        reply.put(map.get("ReceivedPlayerName"), map.get("PlayerName"));
                    } else if (map.get("Players") != null) {
                        List<String> list = new ArrayList<>(Arrays.asList(map.get("Players").split(",")));
                        players.put(map.get("ReceiveServerName"), list);
                    } else if (map.get("ChannelName") != null) {
                        if (!ExistsChannel) { return; }
                        String channelformat;
                        if(map.get("ChannelFormat") == null) {
                            channelformat = setColor(map.get("LunaChannelFormat"));
                            channelformat = channelformat.replace("%prefix", ((map.get("LuckPermsPrefix") == null ? "" : map.get("LuckPermsPrefix")) +
                                    (map.get("RyuZUMapPrefix") == null ? "" : map.get("RyuZUMapPrefix")) +
                                    (map.get("LunaChatPrefix") == null ? "" : map.get("LunaChatPrefix"))))
                                    .replace("%suffix", ((map.get("LuckPermsSuffix") == null ? "" : map.get("LuckPermsSuffix")) +
                                            (map.get("RyuZUMapSuffix") == null ? "" : map.get("RyuZUMapSuffix")) +
                                            (map.get("LunaChatSuffix") == null ? "" : map.get("LunaChatSuffix"))))
                                    .replace("%username", (map.get("PlayerDisplayName") == null ? (map.get("PlayerName") == null ? "" : map.get("PlayerName")) : map.get("PlayerDisplayName")))
                                    .replace("%msg", map.get("[Message]"))
                                    .replace("%premsg", map.get("[PreReplaceMessage]"));
                            msg = channelformat;
                        } else {
                            channelformat = setColor(map.get("ChannelFormat"))
                                    .replace("[ChannelName]", (map.get("ChannelName") == null ? "" : map.get("ChannelName")))
                                    .replace("[ChannelAliasChannelAlias]", (map.get("ChannelAlias") == null ? "" : map.get("ChannelAlias")))
                                    .replace("[ChannelColorCode]", (map.get("ChannelColorCode") == null ? "" : map.get("ChannelColorCode")));
                            msg = channelformat + msg;
                        }
                        for (Player p : getServer().getOnlinePlayers().stream().filter(p -> lunachannel.getMembers().stream().map(m -> ((ChannelMemberBukkit) m).getPlayer()).collect(Collectors.toList()).contains(p) || p.hasPermission("rpc.op")).collect(Collectors.toList())) { p.sendMessage(msg); }
                        if (map.get("ReceiveServerName").equals(map.get("SendServerName"))) {
                            getLogger().info(msg);
                        } else {
                            getLogger().info("(" + ChatColor.RED + map.get("SendServerName") + ChatColor.WHITE + ")" + map.get("PlayerName") + " --> " + map.get("Message") + ChatColor.BLUE + (map.get("ChannelName") == null ? "" : map.get("ChannelName")));
                        }
                    } else {
                        for (Player p : (ExistsChannel ? getServer().getOnlinePlayers().stream().filter(p -> lunachannel.getMembers().stream().map(m -> ((ChannelMemberBukkit) m).getPlayer()).collect(Collectors.toList()).contains(p)).filter(p -> p.hasPermission("rpc.op")).collect(Collectors.toList()) : getServer().getOnlinePlayers())) {
                            p.sendMessage(msg);
                        }
                        if (map.get("ReceiveServerName").equals(map.get("SendServerName"))) {
                            getLogger().info(msg);
                        } else {
                            getLogger().info("(" + ChatColor.RED + map.get("SendServerName") + ChatColor.WHITE + ")" + map.get("PlayerName") + " --> " + map.get("Message") + ChatColor.BLUE + (map.get("ChannelName") == null ? "" : map.get("ChannelName")));
                        }
                    }
                    break;
                case "Prefix":
                    prefix.put(map.get("PlayerName"), map.get("Prefix"));
                    break;
                case "Suffix":
                    suffix.put(map.get("PlayerName"), map.get("Suffix"));
                    break;
            }
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        boolean global = lunachatapi.getDefaultChannel(p.getName()) == null;
        if(global || e.getMessage().substring(0 , 1).equals("!")) {
            sendGlobalMessage(p , e.getMessage());
        } else {
            sendChannelMessage(p , e.getMessage() , lunachatapi.getDefaultChannel(p.getName()));
        }
        e.setFormat("");
        e.setCancelled(true);
    }

    public void sendChannelMessage(Player p , String message , Channel channel) {
        Map<String , String> map = new HashMap<>();
        map.put("Message" , replaceMessage(message , p).replace("$" , "").replace("#" , "").replace("!" , ""));
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
    }

    public void sendGlobalMessage(Player p , String message) {
        Map<String , String> map = new HashMap<>();
        map.put("Message" , replaceMessage(message , p).replace("$" , "").replace("#" , "").replace("!" , ""));
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
    }

    public void sendPrivateMessage(Player p , String message , String receiver) {
        ChannelMemberBukkit cp =  ChannelMemberBukkit.getChannelMemberBukkit(p.getName());
        Map<String , String> map = new HashMap<>();
        map.put("Message" , replaceMessage(message , p).replace("$" , "").replace("#" , "").replace("!" , ""));
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
    }

    public void sendReturnPrivateMessage(String p , Map<String , String> data) {
        Map<String , String> map = new HashMap<>(data);
        map.put("ReceivedPlayerName" , map.get("ReceivePlayerName"));
        map.remove("ReceivePlayerName");
        map.put("System" , "Chat");
        sendPluginMessage("ryuzuchat:ryuzuchat" , mapToJson(map));
    }

    private String replaceMessage(String msg , Player p) {
        String message = msg;
        LunaChatConfig config = LunaChat.getConfig();
        if(canJapanese(msg , p)) {message = lunachatapi.japanize(message , config.getJapanizeType()); }
        if(config.isEnableNormalChatColorCode() || p.hasPermission("lunachat.allowcc")) {message = setColor(message); }
        return message;
    }

    private boolean canJapanese(String msg , Player p) {
        LunaChatConfig config = LunaChat.getConfig();
        return lunachatapi.isPlayerJapanize(p.getName()) &&
                config.getJapanizeType() != JapanizeType.NONE &&
                msg.getBytes(StandardCharsets.UTF_8).length <= msg.length() &&
                !msg.substring(0 , 1).equals("#") &&
                !msg.substring(0 , 1).equals("$");
    }

    private String setColor(String msg) {
        String replaced = msg;
        replaced = replaceToHexFromRGB(replaced);
        return ChatColor.translateAlternateColorCodes('&' , replaced);
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
        map.put("System" , "Chat");
        map.put("Players" , list);
        sendPluginMessage("ryuzuchat:ryuzuchat" , mapToJson(map));
    }

    public static class CheckPlayers implements Runnable {
        @Override
        public void run() {
            RyuZUPluginChat.ryuzupluginchat.sendPlayers();
        }
    }

}
