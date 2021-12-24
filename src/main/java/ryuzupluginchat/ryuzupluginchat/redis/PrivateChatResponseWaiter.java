package ryuzupluginchat.ryuzupluginchat.redis;

import java.util.HashMap;
import java.util.HashSet;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import ryuzupluginchat.ryuzupluginchat.message.data.PrivateMessageData;

public class PrivateChatResponseWaiter {

  private HashMap<Long, PrivateMessageData> dataMap = new HashMap<>();
  private HashMap<Long, Long> timeouts = new HashMap<>();

  public void register(long id, PrivateMessageData data, long timeout) {
    dataMap.put(id, data);
    timeouts.put(id, System.currentTimeMillis() + timeout);
  }

  public void runTimeoutDetectTask(JavaPlugin plugin) {
    Bukkit.getScheduler().runTaskTimer(plugin, () -> {
      for (long id : new HashSet<>(timeouts.keySet())) {
        if (timeouts.get(id) < System.currentTimeMillis()) {
          dataMap.remove(id);
          timeouts.remove(id);
        }
      }
    }, 10L, 10L);
  }

  protected void reached(long id, String server, String receivedPlayerName) {
    PrivateMessageData data = dataMap.getOrDefault(id, null);
    if (data == null) {
      return;
    }
    data.setReceiveServerName(server);
    data.setReceivedPlayerName(receivedPlayerName);
    String message = data.format();

    Player sentPlayer = Bukkit.getPlayer(data.getSentPlayerName());
    if (sentPlayer == null) {
      return;
    }

    sentPlayer.sendMessage(message);
  }
}
