package ryuzupluginchat.ryuzupluginchat.redis;

import lombok.RequiredArgsConstructor;
import redis.clients.jedis.Jedis;

@RequiredArgsConstructor
public class PrivateChatIDGetter {

  private final Jedis jedis;
  private final String privateChatIdKey;

  public void setup() {
    jedis.setnx(privateChatIdKey, "0");
  }

  public long getNewId() {
    return jedis.incr(privateChatIdKey);
  }
}
