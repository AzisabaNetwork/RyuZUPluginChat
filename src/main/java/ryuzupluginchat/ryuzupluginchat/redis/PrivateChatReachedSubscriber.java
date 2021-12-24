package ryuzupluginchat.ryuzupluginchat.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;
import ryuzupluginchat.ryuzupluginchat.RyuZUPluginChat;

@RequiredArgsConstructor
public class PrivateChatReachedSubscriber {

  private final RyuZUPluginChat plugin;
  private final Jedis jedis;

  private final String channelName;

  private final ObjectMapper mapper = new ObjectMapper();

  public void subscribe() {
    JedisPubSub subscriber = new JedisPubSub() {
      @Override
      public void onMessage(String channel, String message) {
        try {
          Map<String, String> map = mapper.readValue(message,
              new TypeReference<Map<String, String>>() {
              });

          long id = Long.parseLong(map.get("id"));
          String targetName = map.get("target");
          String serverName = map.get("server");

          plugin.getPrivateChatResponseWaiter().reached(id, serverName, targetName);
        } catch (NumberFormatException e) {
          plugin.getLogger()
              .warning("Unknown id received. private chat response id must be a number.");
        } catch (JsonProcessingException e) {
          e.printStackTrace();
        }
      }
    };

    Bukkit.getScheduler().runTaskAsynchronously(plugin,
        () -> jedis.subscribe(subscriber, channelName + ".response"));
  }

  public void close() {
    jedis.close();
  }
}
