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

  private String chatFormat;
  private String serverName;
  private HostAndPort hostAndPort;
  private String redisUserName;
  private String redisPassword;

  private String globalChannel;
  private String privateChannel;
  private String channelChatChannel;
  private String systemChannel;

  private String prefixMapKey;
  private String suffixMapKey;

  private String uuidMapKey;

  public void load() {
    FileConfiguration conf = plugin.getConfig();
    chatFormat = conf.getString("format");
    serverName = conf.getString("serverName");
    hostAndPort = new HostAndPort(conf.getString("redis.hostname"), conf.getInt("redis.port"));
    redisUserName = conf.getString("redis.user");
    redisPassword = conf.getString("redis.password");

    globalChannel = conf.getString("redis.channels.global");
    privateChannel = conf.getString("redis.channels.private");
    channelChatChannel = conf.getString("redis.channels.channelChat");
    systemChannel = conf.getString("redis.channels.system");

    prefixMapKey = conf.getString("redis.keys.prefix");
    suffixMapKey = conf.getString("redis.keys.suffix");

    uuidMapKey = conf.getString("redis.keys.uuid");
  }

  public void setGlobalChatFormat(String format) {
    // TODO implement
  }

  public void setPrivateChatFormat(String format) {
    // TODO implement
  }
}
