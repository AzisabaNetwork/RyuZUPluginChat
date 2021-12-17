package ryuzupluginchat.ryuzupluginchat.util;

import java.util.HashMap;
import java.util.UUID;
import org.bukkit.entity.Player;

public class RyuZUPrefixSuffixContainer {

  private HashMap<UUID, String> prefixes = new HashMap<>();
  private HashMap<UUID, String> suffixes = new HashMap<>();

  public String getPrefix(UUID uuid) {
    return prefixes.getOrDefault(uuid, null);
  }

  public String getPrefix(Player p) {
    return getPrefix(p.getUniqueId());
  }

  public String getSuffix(UUID uuid) {
    return suffixes.getOrDefault(uuid, null);
  }

  public String getSuffix(Player p) {
    return getSuffix(p.getUniqueId());
  }

  public void setPrefix(UUID uuid, String prefix) {
    prefixes.put(uuid, prefix);
  }

  public void setPrefix(Player p, String prefix) {
    setPrefix(p.getUniqueId(), prefix);
  }

  public void setSuffix(UUID uuid, String suffix) {
    suffixes.put(uuid, suffix);
  }

  public void setSuffix(Player p, String suffix) {
    setSuffix(p.getUniqueId(), suffix);
  }
}
