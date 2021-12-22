package ryuzupluginchat.ryuzupluginchat.redis;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;
import ryuzupluginchat.ryuzupluginchat.RyuZUPluginChat;
import ryuzupluginchat.ryuzupluginchat.util.JsonDataConverter;
import ryuzupluginchat.ryuzupluginchat.util.message.ChannelChatMessageData;
import ryuzupluginchat.ryuzupluginchat.util.message.GlobalMessageData;
import ryuzupluginchat.ryuzupluginchat.util.message.PrivateMessageData;
import ryuzupluginchat.ryuzupluginchat.util.message.SystemMessageData;

@RequiredArgsConstructor
public class MessageSubscriber {

  private final RyuZUPluginChat plugin;

  private final Jedis jedis;

  private final String globalChannel;
  private final String privateChannel;
  private final String channelChatChannel;
  private final String systemChannel;

  private final JsonDataConverter converter = new JsonDataConverter();

  private final List<Consumer<GlobalMessageData>> globalChannelConsumers = new ArrayList<>();
  private final List<Consumer<PrivateMessageData>> privateChatConsumers = new ArrayList<>();
  private final List<Consumer<ChannelChatMessageData>> channelChatConsumers = new ArrayList<>();
  private final List<Consumer<SystemMessageData>> systemMessageConsumers = new ArrayList<>();

  public void subscribe() {
    JedisPubSub subscriber = new JedisPubSub() {
      @Override
      public void onMessage(String channel, String message) {
        if (channel.equals(globalChannel)) {
          GlobalMessageData data = converter.convertIntoGlobalMessageData(message);
          if (data != null) {
            globalChannelConsumers.forEach(c -> c.accept(data));
          } else {
            // TODO error log
          }

        } else if (channel.equals(privateChannel)) {
          PrivateMessageData data = converter.convertIntoPrivateMessageData(message);
          if (data != null) {
            privateChatConsumers.forEach(c -> c.accept(data));
          } else {
            // TODO error log
          }

        } else if (channel.equals(channelChatChannel)) {
          ChannelChatMessageData data = converter.convertIntoChannelChatMessageData(message);
          if (data != null) {
            channelChatConsumers.forEach(c -> c.accept(data));
          } else {
            // TODO error log
          }

        } else if (channel.equals(systemChannel)) {
          SystemMessageData data = converter.convertIntoSystemMessageData(message);
          if (data != null) {
            systemMessageConsumers.forEach(c -> c.accept(data));
          } else {
            // TODO error log
          }

        }
      }
    };

    jedis.subscribe(subscriber, globalChannel, privateChannel);
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

  public void close() {
    jedis.close();
  }
}
