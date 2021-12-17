package ryuzupluginchat.ryuzupluginchat;

import com.github.ucchyocean.lc3.LunaChat;
import com.github.ucchyocean.lc3.LunaChatAPI;
import com.github.ucchyocean.lc3.util.Utility;
import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.Getter;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import ryuzupluginchat.ryuzupluginchat.config.RPCConfig;
import ryuzupluginchat.ryuzupluginchat.listener.ChatListener;
import ryuzupluginchat.ryuzupluginchat.redis.MessagePublisher;
import ryuzupluginchat.ryuzupluginchat.redis.MessageSubscriber;
import ryuzupluginchat.ryuzupluginchat.redis.RedisConnectionData;
import ryuzupluginchat.ryuzupluginchat.util.RyuZUPrefixSuffixContainer;

public final class RyuZUPluginChat extends JavaPlugin implements Listener {

  public static LunaChatAPI lunachatapi;
  private static HashMap<String, List<String>> players = new HashMap<>();
  private static final HashMap<String, String> prefix = new HashMap<>();
  private static final HashMap<String, String> suffix = new HashMap<>();
  public static HashMap<String, String> reply = new HashMap<>();
  public static RyuZUPluginChat ryuzupluginchat;

  @Getter
  private RPCConfig rpcConfig;
  @Getter
  private RyuZUPrefixSuffixContainer prefixSuffixContainer = new RyuZUPrefixSuffixContainer();

  @Getter
  private MessagePublisher publisher;
  @Getter
  private MessageSubscriber subscriber;

  @Override
  public void onEnable() {
    ryuzupluginchat = this;

    rpcConfig = new RPCConfig(this);
    rpcConfig.load();

    publisher = new MessagePublisher(
        new RedisConnectionData(rpcConfig.getHostAndPort(), rpcConfig.getRedisUserName(),
            rpcConfig.getRedisPassword()), rpcConfig.getGlobalChannel(),
        rpcConfig.getPrivateChannel(), rpcConfig.getChannelChatChannel(),
        rpcConfig.getSystemChannel());
    publisher.connect();

    getServer().getPluginManager().registerEvents(this, this);
    getServer().getPluginManager().registerEvents(new ChatListener(this), this);

    Command command = new Command();
    Tell tell = new Tell();
    Reply reply = new Reply();
    Objects.requireNonNull(getCommand("rpc")).setExecutor(command);
    Objects.requireNonNull(getCommand("rpc")).setTabCompleter(command);
    Objects.requireNonNull(getCommand("tell")).setExecutor(tell);
    Objects.requireNonNull(getCommand("tell")).setTabCompleter(tell);
    Objects.requireNonNull(getCommand("reply")).setExecutor(reply);
    getLogger().info(ChatColor.GREEN + "RyuZUPluginChatが起動しました");
  }

  private String replaceNull(String s) {
    return (s == null ? "" : s);
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
    RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager()
        .getRegistration(LuckPerms.class);
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
    RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager()
        .getRegistration(LuckPerms.class);
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

  private static String replaceToHexFromRGB(String text) {
    String regex = "\\{.+?}";
    List<String> RGBcolors = new ArrayList<>();
    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(text);
    while (matcher.find()) {
      RGBcolors.add(matcher.group());
    }
    for (String hexcolor : RGBcolors) {
      String[] rgbs = hexcolor.replace("{color:", "").replace("}", "").split(",");
      for (int i = 0; i < 3; i++) {
        try {
          if (Integer.parseInt(rgbs[i]) < 0 || Integer.parseInt(rgbs[i]) > 255) {
            return text;
          }
        } catch (NumberFormatException e) {
          return text;
        }
      }
      Color rgb = new Color(Integer.parseInt(rgbs[0]), Integer.parseInt(rgbs[1]),
          Integer.parseInt(rgbs[2]));
      String hex = "#" + Integer.toHexString(rgb.getRGB()).substring(2);
      text = text.replace(hexcolor, ChatColor.of(hex) + "");
    }
    return text;
  }

  public void setFormat(String GroupName, String Format) {
    Map<String, String> map = new HashMap<>();
    map.put("Arg0", GroupName);
    map.put("Arg1", Format);
    map.put("EditTarget", "Format");
    map.put("EditType", "set");
    map.put("System", "EditConfig");
    sendPluginMessage("ryuzuchat:ryuzuchat", mapToJson(map));
  }

  public void setChannelFormat(String GroupName, String Format) {
    Map<String, String> map = new HashMap<>();
    map.put("Arg0", GroupName);
    map.put("Arg1", Format);
    map.put("EditTarget", "ChannelFormat");
    map.put("EditType", "set");
    map.put("System", "EditConfig");
    sendPluginMessage("ryuzuchat:ryuzuchat", mapToJson(map));
  }

  public void setTellFormat(String GroupName, String Format) {
    Map<String, String> map = new HashMap<>();
    map.put("Arg0", GroupName);
    map.put("Arg1", Format);
    map.put("EditTarget", "TellFormat");
    map.put("EditType", "set");
    map.put("System", "EditConfig");
    sendPluginMessage("ryuzuchat:ryuzuchat", mapToJson(map));
  }

  public void addServer(String GroupName, String ServerName) {
    Map<String, String> map = new HashMap<>();
    map.put("Arg0", GroupName);
    map.put("Arg1", ServerName);
    map.put("EditTarget", "List");
    map.put("EditType", "add");
    map.put("System", "EditConfig");
    sendPluginMessage("ryuzuchat:ryuzuchat", mapToJson(map));
  }

  public void removeServer(String GroupName, String ServerName) {
    Map<String, String> map = new HashMap<>();
    map.put("Arg0", GroupName);
    map.put("Arg1", ServerName);
    map.put("EditTarget", "List");
    map.put("EditType", "remove");
    map.put("System", "EditConfig");
    sendPluginMessage("ryuzuchat:ryuzuchat", mapToJson(map));
  }

  public void removeGroup(String GroupName) {
    Map<String, String> map = new HashMap<>();
    map.put("Arg0", GroupName);
    map.put("EditTarget", "Group");
    map.put("EditType", "remove");
    map.put("System", "EditConfig");
    sendPluginMessage("ryuzuchat:ryuzuchat", mapToJson(map));
  }

  public static List<String> getPlayers() {
    List<String> list = new ArrayList<>();
    players.values().forEach(list::addAll);
    return list;
  }

  public void sendPlayers() {
    Map<String, String> map = new HashMap<>();
    String list = StringUtils.join(getServer().getOnlinePlayers().stream().map(HumanEntity::getName)
        .collect(Collectors.toList()), ",");
    map.put("System", "SystemMessage");
    map.put("Players", list);
    sendPluginMessage("ryuzuchat:ryuzuchat", mapToJson(map));
  }

  /**
   * 指定された日付のログファイル名を生成して返します。
   *
   * @param date 日付
   * @return ログファイル名
   */
  private String getFolderPath(Date date) {
    return LunaChat.getDataFolder() +
        File.separator + "logs" +
        File.separator + new SimpleDateFormat("yyyy-MM-dd").format(date);
  }

  /**
   * 指定された日付のログファイルを取得します。 取得できない場合は、nullを返します。
   *
   * @param date 日付
   * @return 指定された日付のログファイル
   */
  private File getLogFile(String date, String channelname) {
    File dir = new File(getFolderPath(new Date()));
    if (!dir.exists() || !dir.isDirectory()) {
      dir.mkdirs();
    }
    File file = new File(dir, channelname + ".log");

    if (date == null) {
      return file;
    }

    Date d;
    try {
      if (date.matches("[0-9]{4}")) {
        date = Calendar.getInstance().get(Calendar.YEAR) + date;
      }
      if (date.matches("[0-9]{8}")) {
        d = new SimpleDateFormat("yyyyMMdd").parse(date);
      } else {
        return null;
      }
    } catch (ParseException e) {
      e.printStackTrace();
      return null;
    }

    File folder = new File(getFolderPath(d));
    if (!folder.exists() || !folder.isDirectory()) {
      return null;
    }

    File f = new File(folder, channelname + ".log");
    if (!f.exists()) {
      return null;
    }

    return f;
  }

  /**
   * ログを出力する
   *
   * @param message ログ内容
   * @param player  発言者名
   */
  public synchronized void addChannelLog(final String message, final String player,
      final String channelname) {
    // 以降の処理を、発言処理の負荷軽減のため、非同期実行にする。(see issue #40.)
    LunaChat.runAsyncTask(() -> {

      String msg = Utility.stripColorCode(message);
      if (msg == null) {
        msg = "";
      }
      msg = msg.replace(",", "，");

      try (OutputStreamWriter writer = new OutputStreamWriter(
          new FileOutputStream(Objects.requireNonNull(getLogFile(null, channelname)), true),
          StandardCharsets.UTF_8);) {

        String str =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "," + msg + ","
                + player;
        writer.write(str + "\r\n");
      } catch (IOException e) {
        e.printStackTrace();
      }
    });
  }
}
