package net.azisaba.ryuzupluginchat.localization;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public abstract class MessageInstance {
  public static final MessageInstance FALLBACK = createSimple(Function.identity());

  public abstract @NotNull String get(@TranslatableKey @NotNull String key);

  @Contract(value = "_ -> new", pure = true)
  public static @NotNull MessageInstance createSimple(@NotNull Function<@NotNull String, @NotNull String> getter) {
    return new MessageInstance() {
      @Override
      public @NotNull String get(@NotNull String key) {
        return getter.apply(key);
      }
    };
  }
}
