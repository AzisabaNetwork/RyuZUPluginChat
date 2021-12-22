package ryuzupluginchat.ryuzupluginchat.redis;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import redis.clients.jedis.Jedis;
import ryuzupluginchat.ryuzupluginchat.RyuZUPluginChat;

@RequiredArgsConstructor
public class RyuZUPrefixSuffixContainer {

  private final Jedis jedis;

  private final String prefixMapKey;
  private final String suffixMapKey;

  public String getPrefix(UUID uuid) {
    return jedis.hget(prefixMapKey, uuid.toString());
  }

  public String getPrefix(Player p) {
    return getPrefix(p.getUniqueId());
  }

  public String getSuffix(UUID uuid) {
    return jedis.hget(suffixMapKey, uuid.toString());
  }

  public String getSuffix(Player p) {
    return getSuffix(p.getUniqueId());
  }

  public void setPrefix(UUID uuid, String prefix, boolean async) {
    if (async) {
      RyuZUPluginChat.newChain()
          .async(() -> setPrefix(uuid, prefix, false))
          .execute();
      return;
    }
    jedis.hset(prefixMapKey, uuid.toString(), prefix);
  }

  public void setPrefix(Player p, String prefix, boolean async) {
    setPrefix(p.getUniqueId(), prefix, async);
  }

  public void setSuffix(UUID uuid, String suffix, boolean async) {
    if (async) {
      RyuZUPluginChat.newChain()
          .async(() -> setSuffix(uuid, suffix, false))
          .execute();
      return;
    }
    jedis.hset(suffixMapKey, uuid.toString(), suffix);
  }

  public void setSuffix(Player p, String suffix, boolean async) {
    setSuffix(p.getUniqueId(), suffix, async);
  }
}
