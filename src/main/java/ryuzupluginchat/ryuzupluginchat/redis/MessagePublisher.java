package ryuzupluginchat.ryuzupluginchat.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import lombok.RequiredArgsConstructor;
import redis.clients.jedis.Jedis;
import ryuzupluginchat.ryuzupluginchat.message.JsonDataConverter;
import ryuzupluginchat.ryuzupluginchat.message.data.ChannelChatMessageData;
import ryuzupluginchat.ryuzupluginchat.message.data.GlobalMessageData;
import ryuzupluginchat.ryuzupluginchat.message.data.PrivateMessageData;
import ryuzupluginchat.ryuzupluginchat.message.data.SystemMessageData;

@RequiredArgsConstructor
public class MessagePublisher {

  private final Jedis jedis;
  private final JsonDataConverter converter;

  private final String groupName;


  public boolean publishGlobalMessage(GlobalMessageData data) {
    try {
      String jsonMessage = converter.convertIntoString(data);

      jedis.publish("rpc:" + groupName + ":global-chat", jsonMessage);
      return true;
    } catch (JsonProcessingException e) {
      e.printStackTrace();
      return false;
    }
  }

  public boolean publishPrivateMessage(PrivateMessageData data) {
    try {
      String jsonMessage = converter.convertIntoString(data);

      jedis.publish("rpc:" + groupName + ":private-chat", jsonMessage);
      return true;
    } catch (JsonProcessingException e) {
      e.printStackTrace();
      return false;
    }
  }

  public boolean publishChannelChatMessage(ChannelChatMessageData data) {
    try {
      String jsonMessage = converter.convertIntoString(data);

      jedis.publish("rpc:" + groupName + ":channel-chat", jsonMessage);
      return true;
    } catch (JsonProcessingException e) {
      e.printStackTrace();
      return false;
    }
  }

  public boolean publishSystemMessage(SystemMessageData data) {
    try {
      String jsonMessage = converter.convertIntoString(data);

      jedis.publish("rpc:" + groupName + ":system-message", jsonMessage);
      return true;
    } catch (JsonProcessingException e) {
      e.printStackTrace();
      return false;
    }
  }

  public void notifyPrivateChatReached(long id, String serverName, String receivedPlayerName) {
    ObjectMapper mapper = new ObjectMapper();
    HashMap<String, Object> map = new HashMap<>();

    map.put("id", id);
    map.put("server", serverName);
    map.put("target", receivedPlayerName);

    try {
      jedis.publish("rpc:" + groupName + ":private-chat-response", mapper.writeValueAsString(map));
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
  }

  public void close() {
    jedis.close();
  }
}
