package ryuzupluginchat.ryuzupluginchat.redis;

import lombok.RequiredArgsConstructor;
import redis.clients.jedis.Jedis;

@RequiredArgsConstructor
public class VCLunaChatChannelSharer {

  private final Jedis jedis;
  private final String groupName;

  private String lunaChatChannelNameCache;
  private long lastFetched = 0L;

  public void setLunaChatChannelName(String name) {
    jedis.set("rpc:" + groupName + ":vc-lunachat-channel", name);

    lastFetched = System.currentTimeMillis();
    lunaChatChannelNameCache = name;
  }

  public String getLunaChatChannelName() {
    if (lunaChatChannelNameCache == null || lastFetched + 10000L < System.currentTimeMillis()) {
      lunaChatChannelNameCache = jedis.get("rpc:" + groupName + ":vc-lunachat-channel");
      lastFetched = System.currentTimeMillis();
    }

    return lunaChatChannelNameCache;
  }
}
