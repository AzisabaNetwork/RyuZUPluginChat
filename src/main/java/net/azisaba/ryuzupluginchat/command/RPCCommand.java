package net.azisaba.ryuzupluginchat.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import net.azisaba.ryuzupluginchat.RyuZUPluginChat;
import net.azisaba.ryuzupluginchat.localization.Messages;
import net.azisaba.ryuzupluginchat.message.InspectHandler;
import net.azisaba.ryuzupluginchat.message.data.SystemMessageData;
import net.azisaba.ryuzupluginchat.util.ArgsConnectUtils;
import net.azisaba.ryuzupluginchat.util.Chat;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@RequiredArgsConstructor
public class RPCCommand implements CommandExecutor, TabCompleter {

  private final RyuZUPluginChat plugin;

  private final List<String> redirectArgs = Arrays.asList("tell", "reply", "hide", "unhide");

  @Override
  public boolean onCommand(
      @NotNull CommandSender sender,
      @NotNull Command command,
      @NotNull String label,
      @NotNull String[] args) {

    if (args.length == 0) {
      sendUsage(sender, label);
      return true;
    }

    if (args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("r")) {
      if (!sender.hasPermission("rpc.op")) {
        Messages.sendFormatted(sender, "command.error.no_permission");
        return true;
      }

      Messages.sendFormatted(sender, "command.rpc.reload.start");
      RyuZUPluginChat.newSharedChain("reload")
          .async(
              () -> {
                long start = System.currentTimeMillis();

                plugin.getRpcConfig().reloadConfig();
                plugin.executeFullReload();

                long end = System.currentTimeMillis();
                Messages.sendFormatted(sender, "command.rpc.reload.done", (end - start));
              })
          .execute();
      return true;
    }

    if (redirectArgs.contains(args[0])) {
      String redirectCommandLabel = ArgsConnectUtils.connect(args);
      Bukkit.dispatchCommand(sender, plugin.getName().toLowerCase() + ":" + redirectCommandLabel);
      return true;
    }

    BiConsumer<String, InspectHandler> toggleSilent = (@Subst("silent") String key, InspectHandler inspectHandler) -> {
      if (!(sender instanceof Player)) {
        Messages.sendFormatted(sender, "command.error.sender_not_player");
        return;
      }
      if (!sender.hasPermission("rpc.op")) {
        Messages.sendFormatted(sender, "command.error.no_permission");
        return;
      }

      Player player = (Player) sender;
      boolean silent = plugin.getPrivateChatInspectHandler().isVisible(player.getUniqueId());

      if (args.length >= 2) {
        switch (args[1].toLowerCase()) {
          case "on":
          case "yes":
          case "enable":
          case "true":
            silent = true;
            break;
          case "off":
          case "no":
          case "disable":
          case "false":
            silent = false;
            break;
          default:
            Messages.sendFormatted(player, "command.error.invalid_single_argument", args[1]);
            return;
        }
      }

      if (silent) {
        plugin.getPrivateChatInspectHandler().setDisable(player.getUniqueId(), true);
        Messages.sendFormatted(player, "command.rpc." + key + ".enabled");
      } else {
        plugin.getPrivateChatInspectHandler().setDisable(player.getUniqueId(), false);
        Messages.sendFormatted(player, "command.rpc." + key + ".disabled");
      }
    };

    if (args[0].equalsIgnoreCase("silent")) {
      toggleSilent.accept("silent", plugin.getPrivateChatInspectHandler());
      return true;
    }

    if (args[0].equalsIgnoreCase("silent-channel")) {
      toggleSilent.accept("silent-channel", plugin.getChannelChatInspectHandler());
      return true;
    }

    if (args[0].equalsIgnoreCase("vanish")) {
      if (!(sender instanceof Player)) {
        Messages.sendFormatted(sender, "command.error.sender_not_player");
        return true;
      }
      if (!sender.hasPermission("rpc.op")) {
        Messages.sendFormatted(sender, "command.error.no_permission");
        return true;
      }

      Player player = (Player) sender;
      boolean vanish = !plugin.getVanishController().isVanished(player.getUniqueId());

      if (vanish) {
        plugin.getVanishController().setVanished(player.getUniqueId(), true);
        Messages.sendFormatted(player, "command.rpc.vanish.enabled");
      } else {
        plugin.getVanishController().setVanished(player.getUniqueId(), false);
        Messages.sendFormatted(player, "command.rpc.vanish.disabled");
      }
      return true;
    }

    if (args[0].equalsIgnoreCase("prefix") || args[0].equalsIgnoreCase("p")) {
      if (!sender.hasPermission("rpc.op")) {
        Messages.sendFormatted(sender, "command.error.no_permission");
        return true;
      }
      if (args.length == 1) {
        Messages.sendFormatted(sender, "command.rpc.prefix.set.usage", label);
        return true;
      }

      if (args[1].equalsIgnoreCase("set")) {
        if (args.length < 3) {
          Messages.sendFormatted(sender, "command.rpc.prefix.set.usage", label);
          return true;
        }
        UUID uuid = plugin.getPlayerUUIDMapContainer().getUUID(args[2]);
        if (uuid == null) {
          Messages.sendFormatted(sender, "command.error.player_not_found", args[2]);
          return true;
        }
        String prefix = args.length > 3 ? ArgsConnectUtils.connect(args, 3) : null;
        plugin.getPrefixSuffixContainer().setPrefix(uuid, prefix, true);
        Messages.sendFormatted(
            sender,
            "command.rpc.prefix.set.success",
            args[2],
            Chat.translateLegacyAmpersand(prefix));
        return true;
      }

      Messages.sendFormatted(sender, "command.rpc.prefix.set.usage", label);
      return true;
    }

    if (args[0].equalsIgnoreCase("suffix") || args[0].equalsIgnoreCase("s")) {
      if (!sender.hasPermission("rpc.op")) {
        Messages.sendFormatted(sender, "command.error.no_permission");
        return true;
      }
      if (args.length == 1) {
        Messages.sendFormatted(sender, "command.rpc.suffix.set.usage", label);
        return true;
      }
      if (args[1].equalsIgnoreCase("set")) {
        if (args.length < 3) {
          Messages.sendFormatted(sender, "command.rpc.suffix.set.usage", label);
          return true;
        }

        UUID uuid = plugin.getPlayerUUIDMapContainer().getUUID(args[2]);
        if (uuid == null) {
          Messages.sendFormatted(sender, "command.error.player_not_found", args[2]);
          return true;
        }

        String suffix = args.length > 3 ? ArgsConnectUtils.connect(args, 3) : null;
        plugin.getPrefixSuffixContainer().setSuffix(uuid, suffix, true);
        Messages.sendFormatted(
            sender,
            "command.rpc.suffix.set.success",
            args[2],
            Chat.translateLegacyAmpersand(suffix));
        return true;
      }

      Messages.sendFormatted(sender, "command.rpc.suffix.set.usage", label);
      return true;
    }

    if (args[0].equalsIgnoreCase("message") || args[0].equalsIgnoreCase("msg")) {
      if (!sender.hasPermission("rpc.op")) {
        Messages.sendFormatted(sender, "command.error.no_permission");
        return true;
      }

      if (args.length <= 2) {
        Messages.sendFormatted(sender, "command.rpc.message.usage", label);
        return true;
      }

      String msg = ArgsConnectUtils.connect(args, 2);
      if (args[1].equalsIgnoreCase("message")) {
        SystemMessageData data =
            plugin.getMessageDataFactory().createGeneralSystemChatMessageData(msg);
        plugin.getPublisher().publishSystemMessage(data);
      } else if (args[1].equalsIgnoreCase("player")) {
        msg = msg.substring(args[2].length() + 1);
        UUID uuid = plugin.getPlayerUUIDMapContainer().getUUID(args[2]);
        if (uuid == null) {
          Messages.sendFormatted(sender, "command.error.player_not_found", args[2]);
          return true;
        }

        SystemMessageData data =
            plugin.getMessageDataFactory().createPrivateSystemChatMessageData(uuid, msg);

        Bukkit.getScheduler()
            .runTaskAsynchronously(plugin, () -> plugin.getPublisher().publishSystemMessage(data));

      } else if (args[1].equalsIgnoreCase("playermessage")) {
        msg = msg.substring(args[2].length() + 1);
        UUID uuid = plugin.getPlayerUUIDMapContainer().getUUID(args[2]);

        if (uuid == null) {
          Messages.sendFormatted(sender, "command.error.player_not_found", args[2]);
          return true;
        }

        SystemMessageData data =
            plugin.getMessageDataFactory().createImitationChatMessageData(uuid, msg);

        Bukkit.getScheduler()
            .runTaskAsynchronously(plugin, () -> plugin.getPublisher().publishSystemMessage(data));
      }
      return true;
    }

    if (args[0].equalsIgnoreCase("config") || args[0].equalsIgnoreCase("c")) {
      if (!sender.hasPermission("rpc.op")) {
        Messages.sendFormatted(sender, "command.error.no_permission");
        return true;
      }
      if (args.length == 1) {
        Messages.sendFormatted(sender, "command.rpc.config.format.set.usage", label);
        return true;
      }

      if (args[1].equalsIgnoreCase("format")) {
        if (args.length <= 4) {
          Messages.sendFormatted(sender, "command.rpc.config.format.set.usage", label);
          return true;
        }

        if (args[2].equalsIgnoreCase("set")) {
          String format = ArgsConnectUtils.connect(args, 4);

          switch (args[3].toLowerCase()) {
            case "global":
            case "all":
            case "default":
              plugin.getRpcConfig().setGlobalChatFormat(format);
              break;
            case "private":
            case "tell":
            case "reply":
              plugin.getRpcConfig().setPrivateChatFormat(format);
              break;
            default:
              Messages.sendFormatted(sender, "command.rpc.config.format.set.usage", label);
              return true;
          }
          Messages.sendFormatted(sender, "command.rpc.config.format.set.success");
          return true;
        }
        return true;
      }
      Messages.sendFormatted(sender, "command.rpc.config.format.set.usage", label);
      return true;
    }

    sendUsage(sender, label);
    return true;
  }

  @Override
  public @Nullable List<String> onTabComplete(
      @NotNull CommandSender sender,
      @NotNull Command command,
      @NotNull String alias,
      @NotNull String[] args) {

    List<String> list = new ArrayList<>();

    if (args.length == 1) {
      if (sender.hasPermission("rpc.op")) {
        list.addAll(Arrays.asList("prefix", "suffix", "message", "config", "silent", "silent-channel", "vanish"));
      }
      list.addAll(redirectArgs);
    }
    if (args.length == 2) {
      if (sender.hasPermission("rpc.op")) {
        if (args[0].equals("prefix") || args[0].equals("suffix")) {
          list.add("set");
        }
        if (args[0].equals("config")) {
          list.addAll(Arrays.asList("format", "channelformat", "tellformat", "list", "group"));
        }
        if (args[0].equals("message")) {
          list.addAll(Arrays.asList("message", "player", "playermessage"));
        }
      }
      if (Arrays.asList("tell", "hide", "unhide").contains(args[0])) {
        if (sender instanceof Player) {
          Player p = (Player) sender;
          list.addAll(
              plugin.getPlayerUUIDMapContainer().getAllNames().stream()
                  .filter(name -> !name.equalsIgnoreCase(p.getName()))
                  .filter(name -> !plugin.getVanishController().isVanished(plugin.getPlayerUUIDMapContainer().getUUID(name)))
                  .collect(Collectors.toList()));
        }
      }
    }
    if (args.length == 3) {
      if (sender.hasPermission("rpc.op")) {
        if (args[1].equals("format")) {
          list.add("set");
        }
        if (args[1].equals("list")) {
          list.addAll(Arrays.asList("add", "remove"));
        }
        if (args[1].equals("group")) {
          list.add("remove");
        }
        if (Arrays.asList("player", "playermessage").contains(args[1])) {
          list.addAll(plugin.getPlayerUUIDMapContainer().getAllNames());
        }
      }
    }
    return list;
  }

  private void sendUsage(CommandSender sender, String label) {
    Messages.sendFormatted(sender, "command.rpc.usage", label);
    if (sender.hasPermission("rpc.op")) {
      Messages.sendFormatted(sender, "command.rpc.usage.op", label);
    }
  }
}
