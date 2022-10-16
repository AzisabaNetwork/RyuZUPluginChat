package net.azisaba.ryuzupluginchat.command;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.OptionalInt;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import net.azisaba.ryuzupluginchat.RyuZUPluginChat;
import net.azisaba.ryuzupluginchat.localization.Messages;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.SetParams;

@RequiredArgsConstructor
public class HideAllCommand implements TabExecutor {
  private static final List<String> DISABLE_WORDS = Arrays.asList("off", "disable", "disabled", "no");
  private static final List<String> SUGGESTIONS = Arrays.asList("off", "disable", "disabled", "no", "30d", "7d", "1d", "1h", "10m", "30s", "1d1h1m1s");
  private final RyuZUPluginChat plugin;

  @Override
  public boolean onCommand(
      @NotNull CommandSender sender,
      @NotNull Command cmd,
      @NotNull String label,
      @NotNull String[] args) {

    if (!(sender instanceof Player)) {
      Messages.sendFormatted(sender, "command.error.sender_not_player");
      return true;
    }
    Player p = (Player) sender;
    Duration duration;

    if (args.length >= 1
        && DISABLE_WORDS.contains(args[0].toLowerCase(Locale.ROOT))) {
      duration = Duration.ZERO;
    } else if (args.length >= 1) {
      try {
        duration = convertStringToDuration(args[0]);
      } catch (IllegalArgumentException ex) {
        Messages.sendFormatted(p, "command.error.invalid_single_argument", args[0]);
        return true;
      }
    } else { // No args
      if (plugin.getHideAllInfoController().isHideAllPlayer(p.getUniqueId())) {
        duration = Duration.ZERO;
      } else {
        duration = Duration.ofMinutes(30);
      }
    }

    if (duration.isZero()) {
      RyuZUPluginChat.newChain()
          .async(
              () -> {
                try (Jedis jedis = plugin.getJedisPool().getResource()) {
                  if (jedis == null) {
                    return;
                  }

                  String key =
                      "rpc:" + plugin.getRpcConfig().getGroupName() + ":hideall:" + p.getUniqueId();
                  jedis.del(key);
                }
                plugin.getHideAllInfoController().discardHideAllInfo(p.getUniqueId());
                Messages.sendFormatted(p, "command.hideall.disabled");
              })
          .execute();
      return true;
    }

    if (duration.isNegative()) {
      Messages.sendFormatted(p, "command.hideall.error.negative");
      return true;
    } else if (duration.getSeconds() > 30 * 24 * 60 * 60) {
      Messages.sendFormatted(p, "command.hideall.error.too_long");
      return true;
    }

    RyuZUPluginChat.newChain()
        .asyncFirst(
            () -> {
              try (Jedis jedis = plugin.getJedisPool().getResource()) {
                if (jedis == null) {
                  return false;
                }

                jedis.set(
                    "rpc:" + plugin.getRpcConfig().getGroupName() + ":hideall:" + p.getUniqueId(),
                    String.valueOf(System.currentTimeMillis() + (duration.getSeconds() * 1000L)),
                    SetParams.setParams().ex(duration.getSeconds()));
                plugin.getHideAllInfoController().refreshHideAllInfoAsync(p.getUniqueId());
                return true;
              }
            })
        .asyncLast(
            success -> {
              if (success) {
                Messages.sendFormatted(p, "command.hideall.enabled", convertDurationToString(p, duration));
              } else {
                Messages.sendFormatted(p, "generic.error");
              }
            })
        .execute();
    return true;
  }

  private Duration convertStringToDuration(String str) throws IllegalArgumentException {
    str = str.toLowerCase(Locale.ROOT);

    Duration duration = Duration.ofSeconds(0);
    while (str.length() > 0) {
      List<Integer> indexes =
          new ArrayList<>(Arrays.asList(str.indexOf("d"), str.indexOf("h"), str.indexOf("m"), str.indexOf("s")));
      indexes.removeIf(i -> i < 0);

      OptionalInt minIndexOptional = indexes.stream().mapToInt(i -> i).min();
      if (!minIndexOptional.isPresent()) {
        throw new IllegalArgumentException();
      }

      int minIndex = minIndexOptional.getAsInt();
      String numStr = str.substring(0, minIndex);
      if (numStr.length() == 0) {
        throw new IllegalArgumentException();
      }

      int amount;
      try {
        amount = Integer.parseInt(numStr);
        if (amount < 0) {
          throw new IllegalArgumentException();
        }
      } catch (NumberFormatException ex) {
        throw new IllegalArgumentException();
      }

      char unit = str.charAt(minIndex);
      if (unit == 'd') {
        duration = duration.plusDays(amount);
      } else if (unit == 'h') {
        duration = duration.plusHours(amount);
      } else if (unit == 'm') {
        duration = duration.plusMinutes(amount);
      } else if (unit == 's') {
        duration = duration.plusSeconds(amount);
      }

      str = str.substring(minIndex + 1);
    }

    return duration;
  }

  private String convertDurationToString(CommandSender sender, Duration duration) {
    String str = "";
    if (duration.toDays() > 0) {
      if (duration.toDays() > 1) {
        str += Messages.getFormattedPlainText(sender, "datetime.days", duration.toDays());
      } else {
        str += Messages.getFormattedPlainText(sender, "datetime.day", duration.toDays());
      }
      duration = duration.minusDays(duration.toDays());
    }
    if (duration.toHours() > 0) {
      if (duration.toHours() > 1) {
        str += Messages.getFormattedPlainText(sender, "datetime.hours", duration.toHours());
      } else {
        str += Messages.getFormattedPlainText(sender, "datetime.hour", duration.toHours());
      }
      duration = duration.minusHours(duration.toHours());
    }
    if (duration.toMinutes() > 0) {
      if (duration.toMinutes() > 1) {
        str += Messages.getFormattedPlainText(sender, "datetime.minutes", duration.toMinutes());
      } else {
        str += Messages.getFormattedPlainText(sender, "datetime.minute", duration.toMinutes());
      }
      duration = duration.minusMinutes(duration.toMinutes());
    }
    if (duration.getSeconds() > 0) {
      if (duration.getSeconds() > 1) {
        str += Messages.getFormattedPlainText(sender, "datetime.seconds", duration.getSeconds());
      } else {
        str += Messages.getFormattedPlainText(sender, "datetime.second", duration.getSeconds());
      }
      //duration = duration.minusSeconds(duration.getSeconds());
    }

    return str.trim();
  }

  @Override
  public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
    if (args.length == 1) {
      return SUGGESTIONS.stream()
          .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
          .collect(Collectors.toList());
    }
    return Collections.emptyList();
  }
}
