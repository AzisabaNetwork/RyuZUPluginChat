package ryuzupluginchat.ryuzupluginchat.redis;

import lombok.RequiredArgsConstructor;
import redis.clients.jedis.Jedis;

@RequiredArgsConstructor
public class PrivateChatIDGetter {

  private final Jedis jedis;
  private final String groupName;

  public void setup() {
    jedis.setnx("rpc:" + groupName + ":private-chat-id-counter", "0");
  }

  public long getNewId() {
    return jedis.incr("rpc:" + groupName + ":private-chat-id-counter");
  }
}
