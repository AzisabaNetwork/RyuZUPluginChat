package net.azisaba.ryuzupluginchat.redis;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import net.azisaba.ryuzupluginchat.util.JedisUtils;
import org.bukkit.entity.Player;
import redis.clients.jedis.JedisPool;

@RequiredArgsConstructor
public class ReplyTargetFetcher {

  private final JedisPool jedisPool;

  private final String groupName;

  public void setReplyTarget(Player p, UUID target) {
    JedisUtils.executeUsingJedisPool(
        jedisPool,
        (jedis) ->
            jedis.hset(
                "rpc:" + groupName + ":reply-map", p.getUniqueId().toString(), target.toString()));
  }

  public UUID getReplyTarget(Player p) {
    String uuidStr =
        JedisUtils.executeUsingJedisPoolWithReturn(
            jedisPool,
            (jedis) -> jedis.hget("rpc:" + groupName + ":reply-map", p.getUniqueId().toString()));
    if (uuidStr == null) {
      return null;
    }
    return UUID.fromString(uuidStr);
  }
}
