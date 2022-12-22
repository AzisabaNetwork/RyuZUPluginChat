package net.azisaba.ryuzupluginchat.event;

import net.azisaba.ryuzupluginchat.message.data.PrivateMessageData;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Set;

public class AsyncPublishPrivateMessageEvent extends AsyncMessageEvent<PrivateMessageData> {
  private static final HandlerList HANDLER_LIST = new HandlerList();

  public AsyncPublishPrivateMessageEvent(@NotNull PrivateMessageData message) {
    super(message, Collections.emptySet());
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
