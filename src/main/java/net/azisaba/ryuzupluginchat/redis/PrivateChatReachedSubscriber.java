package net.azisaba.ryuzupluginchat.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.azisaba.ryuzupluginchat.RyuZUPluginChat;
import net.azisaba.ryuzupluginchat.message.data.PrivateMessageData;
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
              PrivateMessageData data =
                  mapper.readValue(message, new TypeReference<PrivateMessageData>() {});

              plugin.getPrivateChatResponseWaiter().reached(data);
            } catch (JsonProcessingException e) {
              plugin.getSLF4JLogger().error("Failed to process on message", e);
            }
          }

          @Override
          public void onPong(String pattern) {
            Consumer<String> consumer = pingPongQueue.poll();
            if (consumer != null) {
              try {
                consumer.accept(pattern);
              } catch (Exception e) {
                plugin.getSLF4JLogger().error("Failed to process on pong", e);
              }
            }
          }
        };

    executorService = Executors.newFixedThreadPool(1);

    // 初回のみ待機処理無しでタスクを追加する
    executorService.submit(
        () -> {
          try (Jedis jedis = jedisPool.getResource()) {
            jedis.subscribe(subscriber, "rpc:" + groupName + ":private-chat-notify");
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
              jedis.subscribe(subscriber, "rpc:" + groupName + ":private-chat-notify");
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
