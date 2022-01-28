package ryuzupluginchat.ryuzupluginchat.redis;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;
import ryuzupluginchat.ryuzupluginchat.RyuZUPluginChat;
import ryuzupluginchat.ryuzupluginchat.message.JsonDataConverter;
import ryuzupluginchat.ryuzupluginchat.message.data.ChannelChatMessageData;
import ryuzupluginchat.ryuzupluginchat.message.data.GlobalMessageData;
import ryuzupluginchat.ryuzupluginchat.message.data.PrivateMessageData;
import ryuzupluginchat.ryuzupluginchat.message.data.SystemMessageData;

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

  public void subscribe() {
    JedisPubSub subscriber = new JedisPubSub() {
      @Override
      public void onPMessage(String pattern, String channel, String message) {
        if (channel.equals("rpc:" + groupName + ":global-chat")) {
          GlobalMessageData data = converter.convertIntoGlobalMessageData(message);
          if (data != null) {
            globalChannelConsumers.forEach(c -> {
              RyuZUPluginChat.newChain().async(() -> {
                try {
                  c.accept(data);
                } catch (Exception e) {
                  e.printStackTrace();
                }
              }).execute();
            });
          } else {
            // TODO error log
          }

        } else if (channel.equals("rpc:" + groupName + ":private-chat")) {
          PrivateMessageData data = converter.convertIntoPrivateMessageData(message);
          if (data != null) {
            privateChatConsumers.forEach(c -> {
              RyuZUPluginChat.newChain().async(() -> {
                try {
                  c.accept(data);
                } catch (Exception e) {
                  e.printStackTrace();
                }
              }).execute();
            });
          } else {
            // TODO error log
          }

        } else if (channel.equals("rpc:" + groupName + ":channel-chat")) {
          ChannelChatMessageData data = converter.convertIntoChannelChatMessageData(message);
          if (data != null) {
            channelChatConsumers.forEach(c -> {
              RyuZUPluginChat.newChain().async(() -> {
                try {
                  c.accept(data);
                } catch (Exception e) {
                  e.printStackTrace();
                }
              }).execute();
            });
          } else {
            // TODO error log
          }

        } else if (channel.equals("rpc:" + groupName + ":system-message")) {
          SystemMessageData data = converter.convertIntoSystemMessageData(message);
          if (data != null) {
            systemMessageConsumers.forEach(c -> {
              RyuZUPluginChat.newChain().async(() -> {
                try {
                  c.accept(data);
                } catch (Exception e) {
                  e.printStackTrace();
                }
              }).execute();
            });
          } else {
            // TODO error log
          }

        }
      }
    };

    ExecutorService service = Executors.newFixedThreadPool(1);
    for (int i = 0; i < 10000; i++) {
      service.execute(() -> {
        try {

          Jedis jedis = jedisPool.getResource();
          try {
            jedis.psubscribe(subscriber, "rpc:" + groupName + ":*");
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
}
