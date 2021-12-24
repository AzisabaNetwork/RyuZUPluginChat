package ryuzupluginchat.ryuzupluginchat.useful;

import org.bukkit.ChatColor;

public class ColorUtils {

  public static String setColor(String msg) {
    String replaced = msg;
//    replaced = replaceToHexFromRGB(replaced);
    return ChatColor.translateAlternateColorCodes('&', replaced);
  }

//  private static String replaceToHexFromRGB(String text) {
//    String regex = "\\{.+?}";
//    List<String> RGBcolors = new ArrayList<>();
//    Pattern pattern = Pattern.compile(regex);
//    Matcher matcher = pattern.matcher(text);
//    while (matcher.find()) {
//      RGBcolors.add(matcher.group());
//    }
//    for (String hexcolor : RGBcolors) {
//      String[] rgbs = hexcolor.replace("{color:", "").replace("}", "").split(",");
//      for (int i = 0; i < 3; i++) {
//        try {
//          if (Integer.parseInt(rgbs[i]) < 0 || Integer.parseInt(rgbs[i]) > 255) {
//            return text;
//          }
//        } catch (NumberFormatException e) {
//          return text;
//        }
//      }
//      Color rgb = new Color(Integer.parseInt(rgbs[0]), Integer.parseInt(rgbs[1]),
//          Integer.parseInt(rgbs[2]));
//      String hex = "#" + Integer.toHexString(rgb.getRGB()).substring(2);
//      text = text.replace(hexcolor, ChatColor.of(hex) + "");
//    }
//    return text;
//  }
}
