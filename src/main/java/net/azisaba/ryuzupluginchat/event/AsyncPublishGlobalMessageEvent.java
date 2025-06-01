package net.azisaba.ryuzupluginchat.event;

import net.azisaba.ryuzupluginchat.message.data.GlobalMessageData;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

public class AsyncPublishGlobalMessageEvent extends AsyncMessageEvent<GlobalMessageData> {
  private static final HandlerList HANDLER_LIST = new HandlerList();

  public AsyncPublishGlobalMessageEvent(@NotNull GlobalMessageData message) {
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
