package net.azisaba.ryuzupluginchat.command;

import com.github.ucchyocean.lc3.LunaChat;
import com.github.ucchyocean.lc3.channel.Channel;
import com.github.ucchyocean.lc3.member.ChannelMember;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import net.azisaba.ryuzupluginchat.RyuZUPluginChat;
import net.azisaba.ryuzupluginchat.localization.Messages;
import net.azisaba.ryuzupluginchat.message.data.ChannelChatMessageData;
import net.azisaba.ryuzupluginchat.redis.VCLunaChatChannelSharer;
import net.azisaba.ryuzupluginchat.util.ArgsConnectUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class VCCommand implements CommandExecutor {
  private static final long COOLDOWN_SECONDS = 1; // seconds

  private final RyuZUPluginChat plugin;
  private final VCLunaChatChannelSharer vcChannelGetter;

  private List<String> channelMembersMCIDListCache;
  private long cacheLastUpdated = 0L;

  private final HashMap<UUID, Long> lastExecuted = new HashMap<>();

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

    if (args.length == 0) {
      Messages.sendFormatted(sender, "command.vc.usage", label);
      return true;
    }

    Player p = (Player) sender;
    String message = ArgsConnectUtils.connect(args);
    String vcChannelName = vcChannelGetter.getLunaChatChannelName();

    if (lastExecuted.getOrDefault(p.getUniqueId(), 0L) + (COOLDOWN_SECONDS * 1000) > System.currentTimeMillis()) {
      Messages.sendFormatted(p, "command.vc.error.cooldown", COOLDOWN_SECONDS);
      return true;
    }
    lastExecuted.put(p.getUniqueId(), System.currentTimeMillis());

    if (vcChannelName == null) {
      Messages.sendFormatted(p, "generic.error_detailed", Messages.getFormattedPlainText(p, "command.vc.error.not_set_discord_channel_name"));
      return true;
    }

    Channel ch = LunaChat.getAPI().getChannel(vcChannelName);
    if (ch == null) {
      Messages.sendFormatted(p, "generic.error_detailed", Messages.getFormattedPlainText(p, "command.vc.error.channel_not_found"));
      return true;
    }

    if (cacheLastUpdated + 3000L < System.currentTimeMillis()) {
      channelMembersMCIDListCache =
          ch.getMembers().stream().map(ChannelMember::getName).collect(Collectors.toList());
      cacheLastUpdated = System.currentTimeMillis();
    }

    if (!channelMembersMCIDListCache.contains(p.getName())) {
      Messages.sendFormatted(p, "command.vc.error.not_in_channel", ch.getName());
      return true;
    }

    ChannelChatMessageData data =
        plugin.getMessageDataFactory().createChannelChatMessageData(p, vcChannelName, message);
    plugin.getPublisher().publishChannelChatMessage(data);
    return true;
  }
}
