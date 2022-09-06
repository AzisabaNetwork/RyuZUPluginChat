package net.azisaba.ryuzupluginchat.util;

import net.azisaba.ryuzupluginchat.RyuZUPluginChat;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class TaskSchedulingUtils {
  @NotNull
  public static <T> CompletableFuture<T> getSynchronously(@NotNull Supplier<T> supplier) {
    CompletableFuture<T> future = new CompletableFuture<>();
    RyuZUPluginChat.newChain()
        .sync(() -> {
          try {
            future.complete(supplier.get());
          } catch (Throwable t) {
            future.completeExceptionally(t);
          }
        })
        .execute();
    return future;
  }
}
