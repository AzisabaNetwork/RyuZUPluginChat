package ryuzupluginchat.ryuzupluginchat.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import ryuzupluginchat.ryuzupluginchat.util.message.ChannelChatMessageData;
import ryuzupluginchat.ryuzupluginchat.util.message.GlobalMessageData;
import ryuzupluginchat.ryuzupluginchat.util.message.PrivateMessageData;
import ryuzupluginchat.ryuzupluginchat.util.message.SystemMessageData;

public class JsonDataConverter {

  private final ObjectMapper mapper = new ObjectMapper();

  public GlobalMessageData convertIntoGlobalMessageData(String data) {
    return (GlobalMessageData) convertInto(data, GlobalMessageData.class);
  }

  public PrivateMessageData convertIntoPrivateMessageData(String data) {
    return (PrivateMessageData) convertInto(data, PrivateMessageData.class);
  }

  public ChannelChatMessageData convertIntoChannelChatMessageData(String data) {
    return (ChannelChatMessageData) convertInto(data, ChannelChatMessageData.class);
  }

  @SuppressWarnings("unchecked")
  public SystemMessageData convertIntoSystemMessageData(String data) {
    try {
      return new SystemMessageData(null, null,
          (Map<String, Object>) mapper.readValue(data, new TypeReference<Map<String, Object>>() {
          }).get("map"));
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
    return null;
  }

  private Object convertInto(String data, Class<?> clazz) {
    try {
      return mapper.readValue(data, clazz);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
    return null;
  }

  public String convertIntoString(Object o) throws JsonProcessingException {
    return mapper.writeValueAsString(o);
  }
}
