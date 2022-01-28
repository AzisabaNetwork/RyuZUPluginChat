package ryuzupluginchat.ryuzupluginchat.util;

import java.util.function.Consumer;
import java.util.function.Function;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class JedisUtils {

  public static void executeUsingJedisPool(JedisPool pool, Consumer<Jedis> action) {
    Jedis jedis = pool.getResource();
    if (jedis == null) {
      throw new NullPointerException("Failed to establish a connection between redis server");
    }

    try {
      action.accept(jedis);
    } finally {
      jedis.close();
    }
  }

  public static <E> E executeUsingJedisPoolWithReturn(JedisPool pool, Function<Jedis, E> action) {
    Jedis jedis = pool.getResource();
    if (jedis == null) {
      throw new NullPointerException("Failed to establish a connection between redis server");
    }

    try {
      return action.apply(jedis);
    } finally {
      jedis.close();
    }
  }
}
