package net.azisaba.ryuzupluginchat.event;

import net.azisaba.ryuzupluginchat.message.data.PrivateMessageData;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class AsyncPrivateMessageEvent extends AsyncMessageEvent<PrivateMessageData> {
  private static final HandlerList HANDLER_LIST = new HandlerList();

  public AsyncPrivateMessageEvent(@NotNull PrivateMessageData message, @NotNull Set<Player> recipients) {
    super(message, recipients);
  }

  @Override
  public @NotNull HandlerList getHandlers() {
    return HANDLER_LIST;
  }

  @SuppressWarnings("unused")
  @NotNull
  public static HandlerList getHandlerList() {
    return HANDLER_LIST;
  }
}
