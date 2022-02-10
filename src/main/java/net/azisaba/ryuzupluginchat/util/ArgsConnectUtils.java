package net.azisaba.ryuzupluginchat.util;

public class ArgsConnectUtils {

  /**
   * Stringの配列を空白文字で連結します
   *
   * @param args 繋げたいStringの配列
   * @return 空白文字で連結された1つのStringを返す
   */
  public static String connect(String[] args) {
    return String.join(" ", args);
  }

  /**
   * Stringの配列を、指定したindex以降のみ連結します
   *
   * @param args 繋げたいStringの配列
   * @param indexFrom 連結を開始したいindex (返すStringは、このindexのStringから始まる)
   * @return 空白文字で連結された1つのStringを返す
   */
  public static String connect(String[] args, int indexFrom) {
    int sub = indexFrom; // 間にある空白分
    for (int i = 0; i < indexFrom; i++) {
      sub += args[i].length();
    }
    return connect(args).substring(sub);
  }
}
