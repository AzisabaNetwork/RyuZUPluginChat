package net.azisaba.ryuzupluginchat.event;

import net.azisaba.ryuzupluginchat.message.data.ChannelChatMessageData;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

public class AsyncPublishChannelMessageEvent extends AsyncMessageEvent<ChannelChatMessageData> {
  private static final HandlerList HANDLER_LIST = new HandlerList();

  public AsyncPublishChannelMessageEvent(@NotNull ChannelChatMessageData message) {
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
