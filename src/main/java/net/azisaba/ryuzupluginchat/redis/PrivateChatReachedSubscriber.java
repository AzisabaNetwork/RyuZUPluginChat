package net.azisaba.ryuzupluginchat.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.azisaba.ryuzupluginchat.RyuZUPluginChat;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.exceptions.JedisConnectionException;

@RequiredArgsConstructor
public class PrivateChatReachedSubscriber {

  private final RyuZUPluginChat plugin;
  private final JedisPool jedisPool;

  private final String groupName;

  private final ObjectMapper mapper = new ObjectMapper();

  @Getter private JedisPubSub subscriber;
  @Setter @Getter private ExecutorService executorService;

  private final ArrayDeque<Consumer<String>> pingPongQueue = new ArrayDeque<>();

  public void subscribe() {
    subscriber =
        new JedisPubSub() {
          @Override
          public void onMessage(String channel, String message) {
            try {
              Map<String, String> map =
                  mapper.readValue(message, new TypeReference<Map<String, String>>() {});

              long id = Long.parseLong(map.get("id"));
              String targetName = map.get("target");
              String targetDisplayName = map.get("targetDisplayName");
              String serverName = map.get("server");

              plugin.getPrivateChatResponseWaiter().reached(id, serverName, targetName, targetDisplayName);
            } catch (NumberFormatException e) {
              plugin
                  .getLogger()
                  .warning("Unknown id received. private chat response id must be a number.");
            } catch (JsonProcessingException e) {
              e.printStackTrace();
            }
          }

          @Override
          public void onPong(String pattern) {
            Consumer<String> consumer = pingPongQueue.poll();
            if (consumer != null) {
              try {
                consumer.accept(pattern);
              } catch (Exception e) {
                e.printStackTrace();
              }
            }
          }
        };

    executorService = Executors.newFixedThreadPool(1);

    // 初回のみ待機処理無しでタスクを追加する
    executorService.submit(
        () -> {
          try (Jedis jedis = jedisPool.getResource()) {
            jedis.subscribe(subscriber, "rpc:" + groupName + ":private-chat-response");
          }
        });

    // 2回目以降は最初に3秒待機する
    for (int i = 0; i < 10000; i++) {
      executorService.submit(
          () -> {
            try {
              Thread.sleep(3000);
            } catch (InterruptedException e) {
              Thread.currentThread().interrupt();
            }

            try (Jedis jedis = jedisPool.getResource()) {
              jedis.subscribe(subscriber, "rpc:" + groupName + ":private-chat-response");
            }
          });
    }
  }

  public long ping() {
    if (subscriber == null || !subscriber.isSubscribed()) {
      return -2;
    }

    Thread thread =
        new Thread(
            () -> {
              try {
                Thread.sleep(5000);
              } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
              }
            });
    long start = System.currentTimeMillis();

    pingPongQueue.add(arg -> thread.interrupt());
    try {
      subscriber.ping();
    } catch (JedisConnectionException e) {
      return -1;
    }

    try {
      thread.join(3000);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    return System.currentTimeMillis() - start;
  }
}
