package net.azisaba.ryuzupluginchat.util;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class LuckPermsPrefixSuffixUtils {

  public static String getPrefix(Player player) {
    RegisteredServiceProvider<LuckPerms> provider =
        Bukkit.getServicesManager().getRegistration(LuckPerms.class);
    if (provider != null) {
      LuckPerms api = provider.getProvider();
      User user = api.getUserManager().getUser(player.getUniqueId());
      if (user != null) {
        return user.getCachedData().getMetaData().getPrefix();
      }
    }
    return null;
  }

  public static String getSuffix(Player player) {
    RegisteredServiceProvider<LuckPerms> provider =
        Bukkit.getServicesManager().getRegistration(LuckPerms.class);
    if (provider != null) {
      LuckPerms api = provider.getProvider();
      User user = api.getUserManager().getUser(player.getUniqueId());
      if (user != null) {
        return user.getCachedData().getMetaData().getSuffix();
      }
    }
    return null;
  }
}
