package net.azisaba.ryuzupluginchat.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class TestChat {
  @Test
  void expandsHexColorsWithoutChangingOtherCodes() {
    assertEquals(
        "&x&f&F&1&1&9&9red &a green &x&0&0&0&0&0&0black &#GG1199",
        Chat.expandHexColors("&#fF1199red &a green &#000000black &#GG1199"));
    assertEquals(
        "§x§f§F§1§1§9§9red §a green &#GG1199",
        Chat.translateLegacyAmpersand("&#fF1199red &a green &#GG1199"));
  }
}
