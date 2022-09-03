package net.azisaba.ryuzupluginchat.event;

import net.azisaba.ryuzupluginchat.message.data.MessageData;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public abstract class AsyncMessageEvent<T extends MessageData> extends Event implements Cancellable {
  private final T message;
  private final Set<Player> recipients;
  private boolean cancelled = false;

  public AsyncMessageEvent(@NotNull T message, @NotNull Set<Player> recipients) {
    super(true);
    this.message = message;
    this.recipients = recipients;
  }

  @Contract(pure = true)
  @NotNull
  public final T getMessage() {
    return message;
  }

  @Contract(pure = true)
  @NotNull
  public final Set<Player> getRecipients() {
    return recipients;
  }

  @Override
  public boolean isCancelled() {
    return cancelled;
  }

  @Override
  public void setCancelled(boolean cancel) {
    this.cancelled = cancel;
  }
}
