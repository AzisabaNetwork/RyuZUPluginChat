package ryuzupluginchat.ryuzupluginchat.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import redis.clients.jedis.Jedis;
import ryuzupluginchat.ryuzupluginchat.util.JsonDataConverter;
import ryuzupluginchat.ryuzupluginchat.util.message.ChannelChatMessageData;
import ryuzupluginchat.ryuzupluginchat.util.message.GlobalMessageData;
import ryuzupluginchat.ryuzupluginchat.util.message.PrivateMessageData;
import ryuzupluginchat.ryuzupluginchat.util.message.SystemMessageData;

@RequiredArgsConstructor
public class MessagePublisher {

  private final Jedis jedis;

  private final String globalChannel;
  private final String privateChannel;
  private final String channelChatChannel;
  private final String systemChannel;

  private final JsonDataConverter converter = new JsonDataConverter();

  public boolean publishGlobalMessage(GlobalMessageData data) {
    try {
      String jsonMessage = converter.convertIntoString(data);

      jedis.publish(globalChannel, jsonMessage);
      return true;
    } catch (JsonProcessingException e) {
      e.printStackTrace();
      return false;
    }
  }

  public boolean publishPrivateMessage(PrivateMessageData data) {
    try {
      String jsonMessage = converter.convertIntoString(data);

      jedis.publish(privateChannel, jsonMessage);
      return true;
    } catch (JsonProcessingException e) {
      e.printStackTrace();
      return false;
    }
  }

  public boolean publishChannelChatMessage(ChannelChatMessageData data) {
    try {
      String jsonMessage = converter.convertIntoString(data);

      jedis.publish(channelChatChannel, jsonMessage);
      return true;
    } catch (JsonProcessingException e) {
      e.printStackTrace();
      return false;
    }
  }

  public boolean publishSystemMessage(SystemMessageData data) {
    try {
      String jsonMessage = converter.convertIntoString(data);

      jedis.publish(systemChannel, jsonMessage);
      return true;
    } catch (JsonProcessingException e) {
      e.printStackTrace();
      return false;
    }
  }

  public void close() {
    jedis.close();
  }
}
