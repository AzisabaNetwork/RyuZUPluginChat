package net.azisaba.ryuzupluginchat.redis;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.azisaba.ryuzupluginchat.RyuZUPluginChat;
import net.azisaba.ryuzupluginchat.message.JsonDataConverter;
import net.azisaba.ryuzupluginchat.message.data.ChannelChatMessageData;
import net.azisaba.ryuzupluginchat.message.data.GlobalMessageData;
import net.azisaba.ryuzupluginchat.message.data.PrivateMessageData;
import net.azisaba.ryuzupluginchat.message.data.SystemMessageData;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;

@RequiredArgsConstructor
public class MessageSubscriber {

  private final RyuZUPluginChat plugin;
  private final JsonDataConverter converter;

  private final JedisPool jedisPool;

  private final String groupName;

  private final List<Consumer<GlobalMessageData>> globalChannelConsumers = new ArrayList<>();
  private final List<Consumer<PrivateMessageData>> privateChatConsumers = new ArrayList<>();
  private final List<Consumer<ChannelChatMessageData>> channelChatConsumers = new ArrayList<>();
  private final List<Consumer<SystemMessageData>> systemMessageConsumers = new ArrayList<>();

  @Getter @Setter private ExecutorService executorService;

  public void subscribe() {
    JedisPubSub subscriber =
        new JedisPubSub() {
          @Override
          public void onPMessage(String pattern, String channel, String message) {
            RyuZUPluginChat.newChain()
                .async(
                    () -> {
                      if (channel.equals("rpc:" + groupName + ":global-chat")) {
                        GlobalMessageData data = converter.convertIntoGlobalMessageData(message);
                        if (data == null) {
                          return;
                        }
                        for (Consumer<GlobalMessageData> c : globalChannelConsumers) {
                          try {
                            c.accept(data);
                          } catch (Exception e) {
                            e.printStackTrace();
                          }
                        }

                      } else if (channel.equals("rpc:" + groupName + ":private-chat")) {
                        PrivateMessageData data = converter.convertIntoPrivateMessageData(message);
                        if (data == null) {
                          return;
                        }
                        for (Consumer<PrivateMessageData> c : privateChatConsumers) {
                          try {
                            c.accept(data);
                          } catch (Exception e) {
                            e.printStackTrace();
                          }
                        }

                      } else if (channel.equals("rpc:" + groupName + ":channel-chat")) {
                        ChannelChatMessageData data =
                            converter.convertIntoChannelChatMessageData(message);
                        if (data == null) {
                          return;
                        }
                        for (Consumer<ChannelChatMessageData> c : channelChatConsumers) {
                          try {
                            c.accept(data);
                          } catch (Exception e) {
                            e.printStackTrace();
                          }
                        }

                      } else if (channel.equals("rpc:" + groupName + ":system-message")) {
                        SystemMessageData data = converter.convertIntoSystemMessageData(message);
                        if (data == null) {
                          return;
                        }
                        for (Consumer<SystemMessageData> c : systemMessageConsumers) {
                          try {
                            c.accept(data);
                          } catch (Exception e) {
                            e.printStackTrace();
                          }
                        }
                      }
                    })
                .execute();
          }
        };

    executorService = Executors.newFixedThreadPool(1);
    // 初回のみ待機処理無しでタスクを追加する
    executorService.submit(
        () -> {
          try (Jedis jedis = jedisPool.getResource()) {
            jedis.psubscribe(subscriber, "rpc:" + groupName + ":*");
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
              jedis.psubscribe(subscriber, "rpc:" + groupName + ":*");
            }
          });
    }
  }

  public void registerFunctions() {
    globalChannelConsumers.add((data) -> plugin.getMessageProcessor().processGlobalMessage(data));
    privateChatConsumers.add((data) -> plugin.getMessageProcessor().processPrivateMessage(data));
    channelChatConsumers.add(
        (data) -> plugin.getMessageProcessor().processChannelChatMessage(data));
    systemMessageConsumers.add((data) -> plugin.getMessageProcessor().processSystemMessage(data));
  }

  public void registerPublicConsumer(Consumer<GlobalMessageData> consumer) {
    globalChannelConsumers.add(consumer);
  }

  public void registerTellConsumer(Consumer<PrivateMessageData> consumer) {
    privateChatConsumers.add(consumer);
  }

  public void registerChannelChatConsumer(Consumer<ChannelChatMessageData> consumer) {
    channelChatConsumers.add(consumer);
  }

  public void registerSystemChatConsumer(Consumer<SystemMessageData> consumer) {
    systemMessageConsumers.add(consumer);
  }

  public void unregisterAll() {
    globalChannelConsumers.clear();
    privateChatConsumers.clear();
    channelChatConsumers.clear();
    systemMessageConsumers.clear();
  }
}
