package ryuzupluginchat.ryuzupluginchat.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.entity.Player;

public class TabCompletePlayerNameContainer {

  private final HashMap<String, UUID> uuidMap = new HashMap<>();

  public void register(String name, UUID uuid) {
    uuidMap.put(name.toLowerCase(), uuid);
  }

  public void register(Player p) {
    uuidMap.put(p.getName().toLowerCase(), p.getUniqueId());
  }

  public void unregister(String name) {
    uuidMap.remove(name.toLowerCase());
  }

  public void unregister(Player p) {
    uuidMap.remove(p.getName().toLowerCase());
  }

  public UUID getUUID(String name) {
    return uuidMap.getOrDefault(name.toLowerCase(), null);
  }

  public Set<String> getAllNames() {
    return uuidMap.keySet();
  }

  public void clearAndRegisterAll(Map<String, UUID> uuidMap) {
    uuidMap.clear();
    uuidMap.putAll(uuidMap);
  }
}
