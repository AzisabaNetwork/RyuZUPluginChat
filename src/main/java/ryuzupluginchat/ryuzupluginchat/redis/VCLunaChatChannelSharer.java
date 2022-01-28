package ryuzupluginchat.ryuzupluginchat.redis;

import lombok.RequiredArgsConstructor;
import redis.clients.jedis.JedisPool;
import ryuzupluginchat.ryuzupluginchat.util.JedisUtils;

@RequiredArgsConstructor
public class VCLunaChatChannelSharer {

  private final JedisPool jedisPool;
  private final String groupName;

  private String lunaChatChannelNameCache;
  private long lastFetched = 0L;

  public void setLunaChatChannelName(String name) {
    JedisUtils.executeUsingJedisPool(jedisPool,
        (jedis) -> jedis.set("rpc:" + groupName + ":vc-lunachat-channel", name));

    lastFetched = System.currentTimeMillis();
    lunaChatChannelNameCache = name;
  }

  public String getLunaChatChannelName() {
    if (lunaChatChannelNameCache == null || lastFetched + 10000L < System.currentTimeMillis()) {
      lunaChatChannelNameCache = JedisUtils.executeUsingJedisPoolWithReturn(jedisPool,
          (jedis) -> jedis.get("rpc:" + groupName + ":vc-lunachat-channel"));
      lastFetched = System.currentTimeMillis();
    }

    return lunaChatChannelNameCache;
  }
}
