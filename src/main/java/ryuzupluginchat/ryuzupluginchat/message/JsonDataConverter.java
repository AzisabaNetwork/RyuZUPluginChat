package ryuzupluginchat.ryuzupluginchat.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import ryuzupluginchat.ryuzupluginchat.RyuZUPluginChat;
import ryuzupluginchat.ryuzupluginchat.message.data.ChannelChatMessageData;
import ryuzupluginchat.ryuzupluginchat.message.data.GlobalMessageData;
import ryuzupluginchat.ryuzupluginchat.message.data.PrivateMessageData;
import ryuzupluginchat.ryuzupluginchat.message.data.SystemMessageData;

@RequiredArgsConstructor
public class JsonDataConverter {

  private final RyuZUPluginChat plugin;

  private final ObjectMapper mapper = new ObjectMapper();

  public GlobalMessageData convertIntoGlobalMessageData(String data) {
    return (GlobalMessageData) convertInto(data, GlobalMessageData.class);
  }

  public PrivateMessageData convertIntoPrivateMessageData(String data) {
    PrivateMessageData convertedData = (PrivateMessageData) convertInto(data,
        PrivateMessageData.class);
    if (convertedData != null) {
      convertedData.setReceiveServerName(plugin.getRpcConfig().getServerName());
    }
    return convertedData;
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
