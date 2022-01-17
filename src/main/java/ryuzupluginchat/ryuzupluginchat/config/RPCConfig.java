package ryuzupluginchat.ryuzupluginchat.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.configuration.file.FileConfiguration;
import redis.clients.jedis.HostAndPort;
import ryuzupluginchat.ryuzupluginchat.RyuZUPluginChat;

@Getter
@RequiredArgsConstructor
public class RPCConfig {

  private final RyuZUPluginChat plugin;

  private String serverName;
  private HostAndPort hostAndPort;
  private String redisUserName;
  private String redisPassword;

  private String globalChatFormat;
  private String privateChatFormat;

  private String groupName;

  public void load() {
    plugin.saveDefaultConfig();
    FileConfiguration conf = plugin.getConfig();

    serverName = conf.getString("serverName");
    hostAndPort = new HostAndPort(conf.getString("redis.hostname"), conf.getInt("redis.port"));
    redisUserName = conf.getString("redis.user");
    redisPassword = conf.getString("redis.password");

    globalChatFormat = conf.getString("formats.global");
    privateChatFormat = conf.getString("formats.private");

    groupName = conf.getString("redis.group");
  }

  public void setGlobalChatFormat(String format) {
    // TODO implement
  }

  public void setPrivateChatFormat(String format) {
    // TODO implement
  }
}
