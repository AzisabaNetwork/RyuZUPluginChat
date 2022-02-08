package net.azisaba.ryuzupluginchat.redis;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import net.azisaba.ryuzupluginchat.RyuZUPluginChat;
import net.azisaba.ryuzupluginchat.util.JedisUtils;
import org.bukkit.entity.Player;
import redis.clients.jedis.JedisPool;

@RequiredArgsConstructor
public class RyuZUPrefixSuffixContainer {

  private final JedisPool jedisPool;

  private final String groupName;

  public String getPrefix(UUID uuid) {
    return JedisUtils.executeUsingJedisPoolWithReturn(jedisPool,
        (jedis) -> jedis.hget("rpc:" + groupName + ":prefixes", uuid.toString()));
  }

  public String getPrefix(Player p) {
    return getPrefix(p.getUniqueId());
  }

  public String getSuffix(UUID uuid) {
    return JedisUtils.executeUsingJedisPoolWithReturn(jedisPool,
        (jedis) -> jedis.hget("rpc:" + groupName + ":suffixes", uuid.toString()));
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

    JedisUtils.executeUsingJedisPoolWithReturn(jedisPool,
        (jedis) -> jedis.hset("rpc:" + groupName + ":prefixes", uuid.toString(), prefix));
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

    JedisUtils.executeUsingJedisPoolWithReturn(jedisPool,
        (jedis) -> jedis.hset("rpc:" + groupName + ":suffixes", uuid.toString(), suffix));
  }

  public void setSuffix(Player p, String suffix, boolean async) {
    setSuffix(p.getUniqueId(), suffix, async);
  }
}
