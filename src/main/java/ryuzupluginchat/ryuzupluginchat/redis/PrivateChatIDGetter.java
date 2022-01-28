package ryuzupluginchat.ryuzupluginchat.redis;

import lombok.RequiredArgsConstructor;
import redis.clients.jedis.JedisPool;
import ryuzupluginchat.ryuzupluginchat.util.JedisUtils;

@RequiredArgsConstructor
public class PrivateChatIDGetter {

  private final JedisPool jedisPool;
  private final String groupName;

  public void setup() {
    JedisUtils.executeUsingJedisPool(jedisPool,
        (jedis) -> jedis.setnx("rpc:" + groupName + ":private-chat-id-counter", "0"));
  }

  public long getNewId() {
    return JedisUtils.executeUsingJedisPoolWithReturn(jedisPool,
        (jedis) -> jedis.incr("rpc:" + groupName + ":private-chat-id-counter"));
  }
}
