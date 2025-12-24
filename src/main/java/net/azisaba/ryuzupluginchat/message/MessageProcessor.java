package net.azisaba.ryuzupluginchat.message;

import com.github.ucchyocean.lc3.LunaChat;
import com.github.ucchyocean.lc3.LunaChatAPI;
import com.github.ucchyocean.lc3.channel.Channel;
import com.github.ucchyocean.lc3.member.ChannelMemberBukkit;

import java.util.*;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import net.azisaba.ryuzupluginchat.RyuZUPluginChat;
import net.azisaba.ryuzupluginchat.event.AsyncChannelMessageEvent;
import net.azisaba.ryuzupluginchat.event.AsyncGlobalMessageEvent;
import net.azisaba.ryuzupluginchat.event.AsyncPrivateMessageEvent;
import net.azisaba.ryuzupluginchat.event.AsyncPrivateMessageNotifyEvent;
import net.azisaba.ryuzupluginchat.message.data.ChannelChatMessageData;
import net.azisaba.ryuzupluginchat.message.data.GlobalMessageData;
import net.azisaba.ryuzupluginchat.message.data.PrivateMessageData;
import net.azisaba.ryuzupluginchat.message.data.SystemMessageData;
import net.azisaba.ryuzupluginchat.util.*;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public class MessageProcessor {
  // net.kyori.adventure.text.Component
  private static final char[] COMPONENT_CLASS_NAME =
          new char[] {'n', 'e', 't', '.', 'k', 'y', 'o', 'r', 'i', '.', 'a', 'd', 'v', 'e', 'n', 't', 'u', 'r', 'e',
                  '.', 't', 'e', 'x', 't', '.', 'C', 'o', 'm', 'p', 'o', 'n', 'e', 'n', 't'};
  // net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
  private static final char[] LEGACY_COMPONENT_SERIALIZER_CLASS_NAME =
          new char[] {'n', 'e', 't', '.', 'k', 'y', 'o', 'r', 'i', '.', 'a', 'd', 'v', 'e', 'n', 't', 'u', 'r', 'e',
                  '.', 't', 'e', 'x', 't', '.', 's', 'e', 'r', 'i', 'a', 'l', 'i', 'z', 'e', 'r', '.',
                  'l', 'e', 'g', 'a', 'c', 'y', '.', 'L', 'e', 'g', 'a', 'c', 'y', 'C', 'o', 'm', 'p', 'o', 'n', 'e',
                  'n', 't', 'S', 'e', 'r', 'i', 'a', 'l', 'i', 'z', 'e', 'r'};
  private static final LegacyComponentSerializer LEGACY_DESERIALIZER =
          LegacyComponentSerializer.builder()
                  .character('&')
                  .hexColors()
                  .extractUrls()
                  .build();
  private static final LegacyComponentSerializer LEGACY_SERIALIZER_HEX =
          LegacyComponentSerializer.builder()
                  .character('§')
                  .hexColors()
                  .build();
  private static final LegacyComponentSerializer LEGACY_SERIALIZER_UNUSUAL_X_REPEATED_HEX =
          LegacyComponentSerializer.builder()
                  .character('§')
                  .hexColors()
                  .useUnusualXRepeatedCharacterHexFormat()
                  .build();
  private final RyuZUPluginChat plugin;

  private void sendRGBMessage(Player player, String message) {
    RGBStatus rgbStatus = isRGBSupported(player);
    if (rgbStatus == RGBStatus.SERVER_AND_CLIENT || rgbStatus == RGBStatus.SERVER_ONLY) {
      try {
        // Try to invoke Player#sendMessage(Component) in Paper API
        Class<?> legacyComponentSerializerClass = Class.forName(new String(LEGACY_COMPONENT_SERIALIZER_CLASS_NAME));
        Object legacyAmpersand = legacyComponentSerializerClass.getMethod("legacyAmpersand").invoke(null);
        Object component = legacyComponentSerializerClass.getMethod("deserialize", String.class).invoke(legacyAmpersand, message);
        Player.class.getMethod("sendMessage", Class.forName(new String(COMPONENT_CLASS_NAME))).invoke(player, component);
      } catch (ReflectiveOperationException ignored) {
        try {
          player.spigot().sendMessage(ComponentSerializer.parse(GsonComponentSerializer.gson().serialize(LEGACY_DESERIALIZER.deserialize(message))));
        } catch (Exception ignored2) {
          player.sendMessage(LEGACY_SERIALIZER_HEX.serialize(LEGACY_DESERIALIZER.deserialize(message)));
        }
      }
    } else if (rgbStatus == RGBStatus.CLIENT_ONLY) {
      if (Bukkit.getPluginManager().getPlugin("ViaVersion") != null) {
        String json = GsonComponentSerializer.gson().serialize(LEGACY_DESERIALIZER.deserialize(message));
        ViaUtil.sendJsonMessage(player, json);
      } else {
        // no way to send RGB chat :(
        player.sendMessage(LegacyComponentSerializer.legacySection().serialize(LEGACY_DESERIALIZER.deserialize(message)));
      }
    } else {
      player.sendMessage(LegacyComponentSerializer.legacySection().serialize(LEGACY_DESERIALIZER.deserialize(message)));
    }
  }

  public void processGlobalMessage(GlobalMessageData data) {
    String message = data.format();

    UUID senderUUID = data.getPlayerUuid();
    if (senderUUID == null) {
      senderUUID = plugin.getPlayerUUIDMapContainer().getUUID(data.getPlayerName());
    }
    final Set<UUID> deafenPlayers;
    if (senderUUID != null) {
      deafenPlayers = plugin.getHideInfoController().getPlayersWhoHide(senderUUID);
    } else {
      if (!data.isFromDiscord()) {
        plugin.getLogger().warning("Could not get UUID for player " + data.getPlayerName());
      }
      deafenPlayers = Collections.emptySet();
    }

    Set<Player> recipients = Bukkit.getOnlinePlayers().stream()
        .filter(p -> !deafenPlayers.contains(p.getUniqueId()))
        .collect(Collectors.toCollection(HashSet::new));

    AsyncGlobalMessageEvent event = new AsyncGlobalMessageEvent(data, recipients);
    Bukkit.getPluginManager().callEvent(event);
    if (event.isCancelled()) {
      return;
    }

    for (Player player : event.getRecipients()) {
      sendRGBMessage(player, message);
    }

    plugin.getLogger().info("[Global-Chat] " + PlainTextComponentSerializer.plainText().serialize(LegacyComponentSerializer.legacyAmpersand().deserialize(message)));
  }

  public void processChannelChatMessage(ChannelChatMessageData data) {
    String message = data.format();

    LunaChatAPI api = LunaChat.getAPI();
    Channel channel = api.getChannel(data.getLunaChatChannelName());

    if (channel == null) {
      plugin
          .getLogger()
          .warning(
              Chat.f("[Chat] The channel named {0} is not found.", data.getLunaChatChannelName()));
      return;
    }

    plugin
        .getLogger()
        .info(Chat.f("[Channel-Chat] ({0}) {1}", channel.getName(), PlainTextComponentSerializer.plainText().serialize(LegacyComponentSerializer.legacyAmpersand().deserialize(message))));

    Set<Player> recipients;
    if (data.isFromDiscord()) {
      List<Player> channelMembers = channel.getMembers().stream()
          .map(m -> ((ChannelMemberBukkit) m).getPlayer())
          .collect(Collectors.toList());
      recipients = TaskSchedulingUtils.getSynchronously(
          () ->
              Bukkit.getOnlinePlayers().stream()
                  .filter(
                      p -> {
                        if (channelMembers.contains(p)) {
                          return true;
                        }
                        return p.hasPermission("rpc.op") &&
                                plugin.getChannelChatInspectHandler().isVisible(p.getUniqueId());
                      })
                  .collect(Collectors.toCollection(HashSet<Player>::new))
      ).join();

    } else {
      UUID senderUUID = data.getPlayerUuid();
      if (senderUUID == null) {
        senderUUID = plugin.getPlayerUUIDMapContainer().getUUID(data.getPlayerName());
      }
      final Set<UUID> deafenPlayers;
      if (senderUUID != null) {
        deafenPlayers = plugin.getHideInfoController().getPlayersWhoHide(senderUUID);
      } else {
        deafenPlayers = Collections.emptySet();
      }

      recipients = TaskSchedulingUtils.getSynchronously(
          () ->
              Bukkit.getOnlinePlayers().stream()
                  .filter(
                      p -> {
                        if (channel.getMembers().stream()
                            .map(m -> ((ChannelMemberBukkit) m).getPlayer())
                            .collect(Collectors.toList())
                            .contains(p)) {
                          return true;
                        }
                        return p.hasPermission("rpc.op") &&
                                plugin.getChannelChatInspectHandler().isVisible(p.getUniqueId());
                      })
                  .filter(p -> !deafenPlayers.contains(p.getUniqueId()))
                  .collect(Collectors.toCollection(HashSet<Player>::new))
      ).join();
    }

    AsyncChannelMessageEvent event = new AsyncChannelMessageEvent(data, recipients);
    Bukkit.getPluginManager().callEvent(event);
    if (event.isCancelled()) {
      return;
    }

    for (Player player : event.getRecipients()) {
      sendRGBMessage(player, message);
    }
  }

  @Deprecated
  public void processPrivateMessage(PrivateMessageData data) {
    if (!data.isDelivered()) {
      processUndeliveredPrivateMessage(data);
    } else {
      notifyDeliveredPrivateMessage(data);
    }
  }

  public void processUndeliveredPrivateMessage(PrivateMessageData data) {
    assert !data.isDelivered();
    Player targetPlayer = Bukkit.getPlayer(data.getReceivedPlayerUUID());
    if (targetPlayer == null) {
      return;
    }

    data.setReceivedPlayerName(targetPlayer.getName());
    data.setReceivedPlayerDisplayName(targetPlayer.getDisplayName());
    data.setReceiveServerName(plugin.getRpcConfig().getServerName());

    String message = data.format();
    Set<Player> recipients = new HashSet<>();

    UUID senderUUID = data.getSentPlayerUuid();
    if (!plugin.getHideInfoController().isHidingPlayer(targetPlayer.getUniqueId(), senderUUID)) {
      recipients.add(targetPlayer);
    }

    plugin.getLogger().info("[Private-Chat] " + PlainTextComponentSerializer.plainText().serialize(LegacyComponentSerializer.legacyAmpersand().deserialize(message)));

    AsyncPrivateMessageEvent event = new AsyncPrivateMessageEvent(data, recipients);
    Bukkit.getPluginManager().callEvent(event);
    if (event.isCancelled()) {
      return;
    }

    for (Player player : event.getRecipients()) {
      sendRGBMessage(player, message);
    }

    RyuZUPluginChat.newChain()
        .async(
            () -> {
              if (senderUUID == null) {
                return;
              }
              plugin.getReplyTargetFetcher().setReplyTarget(targetPlayer, senderUUID);
            })
        .async(() -> plugin.getPublisher().notifyPrivateChatReached(data))
        .execute();
  }

  public void notifyDeliveredPrivateMessage(PrivateMessageData data) {
    assert data.isDelivered();

    Set<Player> recipients =
        TaskSchedulingUtils.getSynchronously(
                () ->
                    Bukkit.getOnlinePlayers().stream()
                        .filter(p -> p.hasPermission("rpc.op"))
                        .filter(p -> !data.getReceivedPlayerUUID().equals(p.getUniqueId()))
                        .filter(
                            p -> plugin.getPrivateChatInspectHandler().isVisible(p.getUniqueId()))
                        .collect(Collectors.toCollection(HashSet<Player>::new)))
            .join();

    Player sentPlayer = Bukkit.getPlayer(data.getSentPlayerUuid());
    if (sentPlayer != null) {
      recipients.add(sentPlayer);
    }

    AsyncPrivateMessageNotifyEvent event = new AsyncPrivateMessageNotifyEvent(data, recipients);
    Bukkit.getPluginManager().callEvent(event);
    if (event.isCancelled()) {
      return;
    }

    String message = data.format();

    for (Player player : event.getRecipients()) {
      sendRGBMessage(player, message);
    }
  }

  public void processSystemMessage(SystemMessageData data) {
    Map<String, Object> mapData = data.getMap();
    SystemMessageType type;
    try {
      type = SystemMessageType.valueOf((String) mapData.get("type"));
    } catch (IllegalArgumentException e) {
      // TODO error
      return;
    }

    if (type == SystemMessageType.GLOBAL_SYSTEM_MESSAGE) {
      String msg = data.format((String) mapData.get("message"));
      Bukkit.broadcastMessage(msg);

    } else if (type == SystemMessageType.PRIVATE_SYSTEM_MESSAGE) {
      String msg = data.format((String) mapData.get("message"));
      UUID target = UUID.fromString((String) mapData.get("target"));
      Player targetPlayer = Bukkit.getPlayer(target);
      if (targetPlayer == null) {
        return;
      }
      targetPlayer.sendMessage(msg);

    } else if (type == SystemMessageType.IMITATION_CHAT) {
      String msg = data.format((String) mapData.get("message"));
      UUID target = UUID.fromString((String) mapData.get("imitateTo"));
      Player targetPlayer = Bukkit.getPlayer(target);
      if (targetPlayer == null) {
        return;
      }
      boolean global = LunaChat.getAPI().getDefaultChannel(targetPlayer.getName()) == null;
      if (global || msg.charAt(0) == '!') {
        // TODO send global message from targetPlayer
      } else {
        // TODO send channel chat message from targetPlayer
      }
    } else {
      // TODO error
    }
  }

  @SuppressWarnings("unchecked")
  private Map<String, String> convertObjectIntoMap(Object mapObject) {
    return (Map<String, String>) mapObject;
  }

  enum RGBStatus {
    UNSUPPORTED,
    SERVER_AND_CLIENT,
    SERVER_ONLY,
    CLIENT_ONLY,
  }

  private static RGBStatus isRGBSupported(Player player) {
    String version = Bukkit.getVersion();
    boolean modern = !version.contains("MC: 1.15") && !version.contains("MC: 1.14") && !version.contains("MC: 1.13") &&
            !version.contains("MC: 1.12") && !version.contains("MC: 1.11") && !version.contains("MC: 1.10") &&
            !version.contains("MC: 1.9") && !version.contains("MC: 1.8");
    if (Bukkit.getPluginManager().getPlugin("ViaVersion") == null) {
      if (modern) {
        return RGBStatus.SERVER_AND_CLIENT;
      } else {
        return RGBStatus.UNSUPPORTED;
      }
    }
    if (ViaUtil.isRGBSupportedVersion(player)) {
      if (modern) {
        return RGBStatus.SERVER_AND_CLIENT;
      } else {
        return RGBStatus.CLIENT_ONLY;
      }
    } else {
      if (modern) {
        return RGBStatus.SERVER_ONLY;
      } else {
        return RGBStatus.UNSUPPORTED;
      }
    }
  }
}
