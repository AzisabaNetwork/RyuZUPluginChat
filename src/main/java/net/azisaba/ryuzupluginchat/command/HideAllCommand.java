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
import net.azisaba.ryuzupluginchat.util.Chat;
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
      sender.sendMessage(Chat.f("&cこのコマンドはプレイヤーのみ有効です"));
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
        p.sendMessage(Chat.f("&e{0}&cは不正な値です", args[0]));
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
                p.sendMessage(Chat.f("&a全体チャットの非表示を解除しました"));
              })
          .execute();
      return true;
    }

    if (duration.isNegative()) {
      p.sendMessage(Chat.f("&cマイナスの値は指定できません"));
      return true;
    } else if (duration.getSeconds() > 30 * 24 * 60 * 60) {
      p.sendMessage(Chat.f("&c30日より大きい値を指定することはできません"));
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
                p.sendMessage(
                    Chat.f("&a{0}の間全体チャットを&c非表示&aに設定しました", convertDurationToString(duration)));
              } else {
                p.sendMessage(Chat.f("&c全体チャットの非表示に失敗しました"));
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

  private String convertDurationToString(Duration duration) {
    String str = "";
    if (duration.toDays() > 0) {
      str += duration.toDays() + "日";
      duration = duration.minusDays(duration.toDays());
    }
    if (duration.toHours() > 0) {
      str += duration.toHours() + "時間";
      duration = duration.minusHours(duration.toHours());
    }
    if (duration.toMinutes() > 0) {
      str += duration.toMinutes() + "分";
      duration = duration.minusMinutes(duration.toMinutes());
    }
    if (duration.getSeconds() > 0) {
      str += duration.getSeconds() + "秒";
      //duration = duration.minusSeconds(duration.getSeconds());
    }
    return str;
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
