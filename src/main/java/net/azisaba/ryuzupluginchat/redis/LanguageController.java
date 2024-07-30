package net.azisaba.ryuzupluginchat.redis;

import lombok.RequiredArgsConstructor;
import net.azisaba.ryuzupluginchat.RyuZUPluginChat;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

@RequiredArgsConstructor
public class LanguageController {

  private final Map<UUID, String> languages = new HashMap<>();
  private final ReentrantLock lock = new ReentrantLock();

  private final JedisPool jedisPool;
  private final String groupName;

  public @NotNull String getLanguage(@NotNull Player player) {
    String language = getLanguage(player.getUniqueId());
    if (language == null) {
      String locale = player.getLocale().replaceAll("(.+)_.+", "$1");
      if (Locale.forLanguageTag(locale).toLanguageTag().equalsIgnoreCase("und")) {
        language = "ja";
      } else {
        language = locale;
      }
    }
    return language;
  }

  public @Nullable String getLanguage(UUID uuid) {
    if (uuid == null) return Locale.JAPANESE.toLanguageTag();
    lock.lock();
    try {
      return languages.get(uuid);
    } finally {
      lock.unlock();
    }
  }

  public @NotNull String getLanguageOrDefault(UUID uuid, String defaultValue) {
    if (uuid == null) return Locale.JAPANESE.toLanguageTag();
    lock.lock();
    try {
      return languages.getOrDefault(uuid, Locale.JAPANESE.toLanguageTag());
    } finally {
      lock.unlock();
    }
  }

  public void refreshAllAsync() {
    RyuZUPluginChat.newChain()
        .async(
            () -> {
              try (Jedis jedis = jedisPool.getResource()) {
                String keyPrefix =
                    "rpc:" + groupName + ":language:";

                try {
                  lock.lock();
                  languages.clear();
                  for (String s : jedis.keys(keyPrefix + "*")) {
                    try {
                      UUID uuid = UUID.fromString(s.replace(keyPrefix, ""));
                      languages.put(uuid, jedis.get(s));
                    } catch (IllegalArgumentException ignored) {
                    }
                  }
                } finally {
                  lock.unlock();
                }
              }
            })
        .execute();
  }

  public void setLanguage(@NotNull UUID uuid, @NotNull String language) {
    lock.lock();
    try {
      languages.put(uuid, language);
      RyuZUPluginChat.newChain()
          .async(
              () -> {
                try (Jedis jedis = jedisPool.getResource()) {
                  String key =
                      "rpc:" + groupName + ":language:" + uuid;
                  jedis.set(key, language);
                }
              })
          .execute();
    } finally {
      lock.unlock();
    }
  }
}
