package ryuzupluginchat.ryuzupluginchat.listener;

import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import ryuzupluginchat.ryuzupluginchat.RyuZUPluginChat;

@RequiredArgsConstructor
public class OutdatedCommandCaptureListener implements Listener {

  private final RyuZUPluginChat plugin;

  private final List<String> commands = Arrays.asList("/tell", "/reply", "/t", "/r");

  @EventHandler
  public void onCommand(PlayerCommandPreprocessEvent e) {
    String[] labelAndArgs = e.getMessage().split(" ");
    if (labelAndArgs.length <= 0) {
      return;
    }
    String label = labelAndArgs[0];

    if (commands.contains(label.toLowerCase())) {
      labelAndArgs[0] = "/" + plugin.getName().toLowerCase() + ":" + label.substring(1);
    }

    e.setMessage(String.join(" ", labelAndArgs));
  }
}
