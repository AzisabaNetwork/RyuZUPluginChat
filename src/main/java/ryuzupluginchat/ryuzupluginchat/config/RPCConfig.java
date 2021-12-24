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

  private String globalChannel;
  private String privateChannel;
  private String channelChatChannel;
  private String systemChannel;

  private String prefixMapKey;
  private String suffixMapKey;

  private String uuidMapKey;

  private String replyTargetKey;

  public void load() {
    FileConfiguration conf = plugin.getConfig();

    serverName = conf.getString("serverName");
    hostAndPort = new HostAndPort(conf.getString("redis.hostname"), conf.getInt("redis.port"));
    redisUserName = conf.getString("redis.user");
    redisPassword = conf.getString("redis.password");

    globalChatFormat = conf.getString("formats.global");
    privateChatFormat = conf.getString("formats.private");
    
    globalChannel = conf.getString("redis.channels.global");
    privateChannel = conf.getString("redis.channels.private");
    channelChatChannel = conf.getString("redis.channels.channelChat");
    systemChannel = conf.getString("redis.channels.system");

    prefixMapKey = conf.getString("redis.keys.prefix");
    suffixMapKey = conf.getString("redis.keys.suffix");

    uuidMapKey = conf.getString("redis.keys.uuid");

    replyTargetKey = conf.getString("redis.keys.reply");
  }

  public void setGlobalChatFormat(String format) {
    // TODO implement
  }

  public void setPrivateChatFormat(String format) {
    // TODO implement
  }
}
