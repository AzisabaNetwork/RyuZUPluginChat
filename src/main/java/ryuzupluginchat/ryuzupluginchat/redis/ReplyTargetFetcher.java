package ryuzupluginchat.ryuzupluginchat.redis;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import redis.clients.jedis.Jedis;

@RequiredArgsConstructor
public class ReplyTargetFetcher {

  private final Jedis jedis;

  private final String replyCacheKeyName;

  public void setReplyTarget(Player p, UUID target) {
    jedis.hset(replyCacheKeyName, p.getUniqueId().toString(), target.toString());
  }

  public UUID getReplyTarget(Player p) {
    String uuidStr = jedis.hget(replyCacheKeyName, p.getUniqueId().toString());
    return UUID.fromString(uuidStr);
  }

}
