package net.azisaba.ryuzupluginchat.localization;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.experimental.UtilityClass;
import net.azisaba.ryuzupluginchat.util.Functions;
import net.azisaba.ryuzupluginchat.util.MoreObjects;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

@UtilityClass
public final class Messages {
  private static final Gson GSON = new Gson();
  private static final Map<String, MessageInstance> LOCALES = new ConcurrentHashMap<>();
  private static MessageInstance fallback;

  /**
   * Attempts to load all messages from the resources.
   *
   * @throws IOException if an I/O error occurs
   */
  public static void load() throws IOException {
    fallback = MoreObjects.requireNonNullElse(load(Locale.ENGLISH.toLanguageTag()), MessageInstance.FALLBACK);
    LOCALES.put(Locale.ENGLISH.toLanguageTag(), fallback);
    Stream.of(Locale.getAvailableLocales())
        .map(Locale::toLanguageTag)
        .map(s -> s.contains("-") ? s.substring(0, s.indexOf("-")) : s)
        .filter(s -> !s.equals(Locale.ENGLISH.toLanguageTag()))
        .forEach(language -> {
          try {
            MessageInstance instance = Messages.load(language);
            if (instance != null) {
              LOCALES.put(language, instance);
            } else {
              LOCALES.put(language, fallback);
            }
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        });
  }

  /**
   * Load messages from a file.
   *
   * @param language two-char language code, but anything really works.
   * @return the message instance
   * @throws IOException If an I/O error occurs.
   */
  public static @Nullable MessageInstance load(@NotNull String language) throws IOException {
    try (InputStream in = Messages.class.getResourceAsStream("/messages_" + language + ".json")) {
      if (in == null) {
        return null;
      }
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
        JsonObject obj = GSON.fromJson(reader, JsonObject.class);
        return MessageInstance.createSimple(Functions.memoize(s -> {
          JsonElement element = obj.get(s);
          if (element == null) {
            return s;
          } else if (element.isJsonArray()) {
            List<String> list = new ArrayList<>();
            for (JsonElement el : element.getAsJsonArray()) {
              list.add(el.getAsString());
            }
            return String.join("\n", list);
          } else {
            return String.valueOf(element.getAsString());
          }
        }));
      }
    }
  }

  /**
   * Get the message instance for the given locale.
   *
   * @param locale the locale
   * @return the message instance
   */
  public static @NotNull MessageInstance getInstance(@Nullable Locale locale) {
    Objects.requireNonNull(fallback, "messages not loaded yet");
    if (locale == null) {
      return fallback;
    }
    return LOCALES.getOrDefault(locale.toLanguageTag(), fallback);
  }

  /**
   * Gets the message instance for the given command sender.
   *
   * @param sender the command sender
   * @return the message instance
   */
  public static @NotNull MessageInstance getInstanceFor(@Nullable CommandSender sender) {
    if (!(sender instanceof Player)) {
      return getInstance(null);
    }
    String locale = ((Player) sender).getLocale();
    if (locale.contains("_")) {
      locale = locale.substring(0, locale.indexOf("_"));
    }
    return getInstance(Locale.forLanguageTag(locale));
  }

  /**
   * Gets the message by key and formats the text using the provided arguments.
   *
   * @param sender the sender to get the locale from
   * @param key    the translatable key of the message
   * @param args   the arguments to format the message with
   * @return the formatted message with legacy color codes
   */
  public static @NotNull String getFormattedText(@Nullable CommandSender sender, @TranslatableKey @NotNull String key, @Nullable Object @NotNull ... args) {
    return MessageFormat.format(ChatColor.translateAlternateColorCodes('&', getInstanceFor(sender).get(key)), args);
  }

  /**
   * Gets the message by key and formats the text using the provided arguments.
   *
   * @param sender the sender to get the locale from
   * @param key    the translatable key of the message
   * @param args   the arguments to format the message with
   * @return the formatted message, without color codes
   */
  public static @NotNull String getFormattedPlainText(@Nullable CommandSender sender, @TranslatableKey @NotNull String key, @Nullable Object @NotNull ... args) {
    return ChatColor.stripColor(getFormattedText(sender, key, args));
  }

  /**
   * Sends the formatted message (formatted message with legacy color code) to the provided sender.
   *
   * @param sender the sender to get the locale from, and to send the message
   * @param key    the translatable key of the message
   * @param args   the arguments to format the message with
   */
  public static void sendFormatted(@NotNull CommandSender sender, @TranslatableKey @NotNull String key, @Nullable Object @NotNull ... args) {
    sender.sendMessage(getFormattedText(sender, key, args));
  }
}
