package net.azisaba.ryuzupluginchat.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import lombok.RequiredArgsConstructor;
import net.azisaba.ryuzupluginchat.RyuZUPluginChat;
import net.azisaba.ryuzupluginchat.message.JsonDataConverter;
import net.azisaba.ryuzupluginchat.message.data.ChannelChatMessageData;
import net.azisaba.ryuzupluginchat.message.data.GlobalMessageData;
import net.azisaba.ryuzupluginchat.message.data.PrivateMessageData;
import net.azisaba.ryuzupluginchat.message.data.SystemMessageData;
import net.azisaba.ryuzupluginchat.util.JedisUtils;
import redis.clients.jedis.JedisPool;

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

    JedisUtils.executeUsingJedisPool(
        jedisPool, (jedis) -> jedis.publish("rpc:" + groupName + ":global-chat", jsonMessage));
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

    JedisUtils.executeUsingJedisPool(
        jedisPool, (jedis) -> jedis.publish("rpc:" + groupName + ":private-chat", jsonMessage));
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

    JedisUtils.executeUsingJedisPool(
        jedisPool, (jedis) -> jedis.publish("rpc:" + groupName + ":channel-chat", jsonMessage));
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

    JedisUtils.executeUsingJedisPool(
        jedisPool, (jedis) -> jedis.publish("rpc:" + groupName + ":system-message", jsonMessage));
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

    JedisUtils.executeUsingJedisPool(
        jedisPool,
        (jedis) -> jedis.publish("rpc:" + groupName + ":private-chat-response", jsonMessage));
  }
}
