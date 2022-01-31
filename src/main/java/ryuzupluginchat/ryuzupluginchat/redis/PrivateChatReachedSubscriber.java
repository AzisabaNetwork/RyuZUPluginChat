package ryuzupluginchat.ryuzupluginchat.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;
import ryuzupluginchat.ryuzupluginchat.RyuZUPluginChat;

@RequiredArgsConstructor
public class PrivateChatReachedSubscriber {

  private final RyuZUPluginChat plugin;
  private final JedisPool jedisPool;

  private final String groupName;

  private final ObjectMapper mapper = new ObjectMapper();

  @Setter
  @Getter
  private ExecutorService executorService;

  public void subscribe() {
    JedisPubSub subscriber = new JedisPubSub() {
      @Override
      public void onMessage(String channel, String message) {
        try {
          Map<String, String> map = mapper.readValue(message,
              new TypeReference<Map<String, String>>() {
              });

          long id = Long.parseLong(map.get("id"));
          String targetName = map.get("target");
          String serverName = map.get("server");

          plugin.getPrivateChatResponseWaiter().reached(id, serverName, targetName);
        } catch (NumberFormatException e) {
          plugin.getLogger()
              .warning("Unknown id received. private chat response id must be a number.");
        } catch (JsonProcessingException e) {
          e.printStackTrace();
        }
      }
    };

    executorService = Executors.newFixedThreadPool(1);

    // 初回のみ待機処理無しでタスクを追加する
    executorService.execute(() -> {
      try (Jedis jedis = jedisPool.getResource()) {
        jedis.psubscribe(subscriber, "rpc:" + groupName + ":private-chat-response");
      }
    });

    // 2回目以降は最初に3秒待機する
    for (int i = 0; i < 10000; i++) {
      executorService.execute(() -> {
        try {
          Thread.sleep(3000);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }

        try (Jedis jedis = jedisPool.getResource()) {
          jedis.psubscribe(subscriber, "rpc:" + groupName + ":private-chat-response");
        }
      });
    }
  }
}
