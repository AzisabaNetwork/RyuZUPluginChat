package ryuzupluginchat.ryuzupluginchat.redis;

import java.util.HashMap;
import java.util.UUID;
import org.bukkit.entity.Player;

public class ReplyMessageCacheContainer {

  private final HashMap<UUID, String> replyTargetMap = new HashMap<>();

  public void setReplyPlayer(Player p, String targetPlayerName) {
    replyTargetMap.put(p.getUniqueId(), targetPlayerName);
  }

  public String getReplyPlayer(Player p) {
    return replyTargetMap.getOrDefault(p.getUniqueId(), null);
  }

}
