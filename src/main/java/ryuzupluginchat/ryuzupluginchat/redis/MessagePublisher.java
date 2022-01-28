package ryuzupluginchat.ryuzupluginchat.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import lombok.RequiredArgsConstructor;
import redis.clients.jedis.JedisPool;
import ryuzupluginchat.ryuzupluginchat.RyuZUPluginChat;
import ryuzupluginchat.ryuzupluginchat.message.JsonDataConverter;
import ryuzupluginchat.ryuzupluginchat.message.data.ChannelChatMessageData;
import ryuzupluginchat.ryuzupluginchat.message.data.GlobalMessageData;
import ryuzupluginchat.ryuzupluginchat.message.data.PrivateMessageData;
import ryuzupluginchat.ryuzupluginchat.message.data.SystemMessageData;
import ryuzupluginchat.ryuzupluginchat.util.JedisUtils;

@RequiredArgsConstructor
public class MessagePublisher {

  private final RyuZUPluginChat plugin;

  private final JedisPool jedisPool;
  private final JsonDataConverter converter;

  private final String groupName;


  public boolean publishGlobalMessage(GlobalMessageData data) {
    String jsonMessage;
    try {
      jsonMessage = converter.convertIntoString(data);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
      return false;
    }

    JedisUtils.executeUsingJedisPool(jedisPool,
        (jedis) -> jedis.publish("rpc:" + groupName + ":global-chat", jsonMessage));
    return true;
  }

  public boolean publishPrivateMessage(PrivateMessageData data) {
    String jsonMessage;
    try {
      jsonMessage = converter.convertIntoString(data);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
      return false;
    }

    JedisUtils.executeUsingJedisPool(jedisPool,
        (jedis) -> jedis.publish("rpc:" + groupName + ":private-chat", jsonMessage));
    return true;
  }

  public boolean publishChannelChatMessage(ChannelChatMessageData data) {
    String jsonMessage;
    try {
      jsonMessage = converter.convertIntoString(data);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
      return false;
    }

    JedisUtils.executeUsingJedisPool(jedisPool,
        (jedis) -> jedis.publish("rpc:" + groupName + ":channel-chat", jsonMessage));
    return true;
  }

  public boolean publishSystemMessage(SystemMessageData data) {
    String jsonMessage;
    try {
      jsonMessage = converter.convertIntoString(data);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
      return false;
    }

    JedisUtils.executeUsingJedisPool(jedisPool,
        (jedis) -> jedis.publish("rpc:" + groupName + ":system-message", jsonMessage));
    return true;
  }

  public void notifyPrivateChatReached(long id, String serverName, String receivedPlayerName) {
    ObjectMapper mapper = new ObjectMapper();
    HashMap<String, Object> map = new HashMap<>();

    map.put("id", id);
    map.put("server", serverName);
    map.put("target", receivedPlayerName);

    String jsonMessage;
    try {
      jsonMessage = mapper.writeValueAsString(map);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
      return;
    }

    JedisUtils.executeUsingJedisPool(jedisPool,
        (jedis) -> jedis.publish("rpc:" + groupName + ":private-chat-response", jsonMessage));
  }
}
