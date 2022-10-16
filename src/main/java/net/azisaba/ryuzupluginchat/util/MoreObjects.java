package net.azisaba.ryuzupluginchat.util;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@UtilityClass
public class MoreObjects {
  @Contract(value = "null, _ -> param2; !null, _ -> param1", pure = true)
  public static <T> T requireNonNullElse(@Nullable T t, @NotNull T defaultValue) {
    if (t == null) {
      return defaultValue;
    }
    return t;
  }
}
