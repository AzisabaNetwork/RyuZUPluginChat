package net.azisaba.ryuzupluginchat.redis;

import lombok.RequiredArgsConstructor;
import net.azisaba.ryuzupluginchat.util.JedisUtils;
import redis.clients.jedis.JedisPool;

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
