package net.azisaba.ryuzupluginchat.util;

import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.experimental.UtilityClass;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.Nullable;

@UtilityClass
public class Chat {
  private static final Pattern HEX_COLOR = Pattern.compile("&#([0-9a-fA-F]{6})");

  // メッセージをフォーマットして、&で色をつける
  public static String f(String text, Object... args) {
    return MessageFormat.format(translateLegacyAmpersand(text), args);
  }

  public static String translateLegacyAmpersand(@Nullable String text) {
    if (text == null) {
      return "null";
    }
    return ChatColor.translateAlternateColorCodes('&', replaceHexColors(text, '§'));
  }

  public static String expandHexColors(String text) {
    return replaceHexColors(text, '&');
  }

  private static String replaceHexColors(String text, char colorChar) {
    Matcher matcher = HEX_COLOR.matcher(text);
    StringBuffer result = new StringBuffer();
    while (matcher.find()) {
      String hex = matcher.group(1);
      StringBuilder color = new StringBuilder().append(colorChar).append('x');
      for (int i = 0; i < hex.length(); i++) {
        color.append(colorChar).append(hex.charAt(i));
      }
      matcher.appendReplacement(result, color.toString());
    }
    matcher.appendTail(result);
    return result.toString();
  }

  // 色を消す
  public static String r(String text) {
    return ChatColor.stripColor(text);
  }
}
