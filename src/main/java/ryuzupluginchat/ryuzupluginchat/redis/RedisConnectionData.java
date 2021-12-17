package ryuzupluginchat.ryuzupluginchat.redis;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import redis.clients.jedis.HostAndPort;

@Getter
@RequiredArgsConstructor
public class RedisConnectionData {

  private final HostAndPort hostAndPort;
  private final String user;
  private final String password;
  
}
