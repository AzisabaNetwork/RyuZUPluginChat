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
import net.azisaba.ryuzupluginchat.message.data.*;
import net.azisaba.ryuzupluginchat.util.Chat;
import net.azisaba.ryuzupluginchat.util.OpenAIUtils;
import net.azisaba.ryuzupluginchat.util.TaskSchedulingUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@RequiredArgsConstructor
public class MessageProcessor {

  private final RyuZUPluginChat plugin;

  private void translate(
          @NotNull MessageData data,
          @NotNull String originalMessage,
          @NotNull String from,
          @NotNull String to,
          @Nullable UUID senderUUID
  ) {
    if (from.equals(to)) {
      data.setMessage(originalMessage);
    } else {
      try {
        String displayLanguage = Locale.forLanguageTag(to).getDisplayLanguage(Locale.ENGLISH);
        data.setMessage(OpenAIUtils.ask(plugin.getRpcConfig(), displayLanguage, String.valueOf(senderUUID), originalMessage));
      } catch (Exception e) {
        plugin.getSLF4JLogger().error("Error retrieving translation", e);
        data.setMessage(originalMessage);
      }
    }
  }

  public void processGlobalMessage(GlobalMessageData data) {
    String message = data.format();

    UUID senderUUID =
            data.getPlayerUuid() == null
                    ? plugin.getPlayerUUIDMapContainer().getUUID(data.getPlayerName())
                    : data.getPlayerUuid();
    final Set<UUID> deafenPlayers;
    if (senderUUID != null) {
      deafenPlayers = plugin.getHideInfoController().getPlayersWhoHide(senderUUID);
    } else {
      if (!data.isFromDiscord()) {
        plugin.getLogger().warning("Could not get UUID for player " + data.getPlayerName());
      }
      deafenPlayers = Collections.emptySet();
    }

    plugin.getLogger().info("[Global-Chat] " + ChatColor.stripColor(message));

    String senderLanguage = plugin.getLanguageController().getLanguageOrDefault(senderUUID, Locale.JAPANESE.toLanguageTag());
    Map<String, Set<Player>> recipientsMap = new HashMap<>();
    Bukkit.getOnlinePlayers().stream()
            .filter(p -> !deafenPlayers.contains(p.getUniqueId()))
            .forEach(player -> recipientsMap.computeIfAbsent(plugin.getLanguageController().getLanguage(player), k -> new HashSet<>()).add(player));
    String originalMessage = data.getMessage();
    recipientsMap.entrySet().parallelStream().forEach(entry -> {
      translate(data, originalMessage, senderLanguage, entry.getKey(), senderUUID);
      AsyncGlobalMessageEvent event = new AsyncGlobalMessageEvent(data, entry.getValue());
      Bukkit.getPluginManager().callEvent(event);
      if (event.isCancelled()) {
        return;
      }

      for (Player player : event.getRecipients()) {
        player.sendMessage(message);
      }
    });
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
        .info(Chat.f("[Channel-Chat] ({0}) {1}", channel.getName(), ChatColor.stripColor(message)));

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
      player.sendMessage(message);
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

    plugin.getLogger().info("[Private-Chat] " + ChatColor.stripColor(message));

    AsyncPrivateMessageEvent event = new AsyncPrivateMessageEvent(data, recipients);
    Bukkit.getPluginManager().callEvent(event);
    if (event.isCancelled()) {
      return;
    }

    for (Player player : event.getRecipients()) {
      player.sendMessage(message);
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
      player.sendMessage(message);
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
}
