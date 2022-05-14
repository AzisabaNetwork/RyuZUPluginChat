package net.azisaba.ryuzupluginchat.task;

import lombok.RequiredArgsConstructor;
import net.azisaba.ryuzupluginchat.RyuZUPluginChat;
import net.azisaba.ryuzupluginchat.redis.MessageSubscriber;
import net.azisaba.ryuzupluginchat.redis.PrivateChatReachedSubscriber;
import org.bukkit.Bukkit;

@RequiredArgsConstructor
public class SubscriberPingTask {

  private final RyuZUPluginChat plugin;

  private final MessageSubscriber messageSubscriber;
  private final PrivateChatReachedSubscriber privateChatReachedSubscriber;

  public void run() {
    Bukkit.getScheduler()
        .runTaskTimerAsynchronously(
            plugin,
            () -> {
              long latency = messageSubscriber.ping();
              if (latency < 0) {
                plugin.getLogger().info("Lost connection to the redis server. Restarting...");
                messageSubscriber.getSubscriber().punsubscribe();
              }

              latency = privateChatReachedSubscriber.ping();
              if (latency < 0) {
                plugin.getLogger().info("Lost connection to the redis server. Restarting...");
                privateChatReachedSubscriber.getSubscriber().punsubscribe();
              }
            },
            20 * 5,
            20 * 5);
  }
}
