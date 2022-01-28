package ryuzupluginchat.ryuzupluginchat.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.RequiredArgsConstructor;
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

    ExecutorService service = Executors.newFixedThreadPool(1);
    for (int i = 0; i < 10000; i++) {
      service.execute(() -> {
        try {

          Jedis jedis = jedisPool.getResource();
          try {
            jedis.subscribe(subscriber, "rpc:" + groupName + ":private-chat-response");
          } finally {
            jedis.close();
          }

        } finally {
          // 接続に失敗したら3秒待ってから再接続する
          try {
            Thread.sleep(3000L);
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
          }
        }
      });
    }
  }
}
