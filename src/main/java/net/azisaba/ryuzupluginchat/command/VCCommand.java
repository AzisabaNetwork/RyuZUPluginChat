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
import net.azisaba.ryuzupluginchat.message.data.ChannelChatMessageData;
import net.azisaba.ryuzupluginchat.redis.VCLunaChatChannelSharer;
import net.azisaba.ryuzupluginchat.util.ArgsConnectUtils;
import net.azisaba.ryuzupluginchat.util.Chat;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class VCCommand implements CommandExecutor {

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
      sender.sendMessage(Chat.f("&cこのコマンドはプレイヤーのみ実行可能です"));
      return true;
    }

    if (args.length <= 0) {
      sender.sendMessage(Chat.f("&c使い方: /{0} <メッセージ>", label));
      return true;
    }

    Player p = (Player) sender;
    String message = ArgsConnectUtils.connect(args);
    String vcChannelName = vcChannelGetter.getLunaChatChannelName();

    if (lastExecuted.getOrDefault(p.getUniqueId(), 0L) + 1000L > System.currentTimeMillis()) {
      p.sendMessage(Chat.f("&cクールタイム中です。1秒間待って実行してください。"));
      return true;
    }
    lastExecuted.put(p.getUniqueId(), System.currentTimeMillis());

    if (vcChannelName == null) {
      p.sendMessage(Chat.f("&cエラーが発生しました (Discordと連携するLunaChatチャンネルが不明です)"));
      return true;
    }

    Channel ch = LunaChat.getAPI().getChannel(vcChannelName);
    if (ch == null) {
      p.sendMessage(Chat.f("&cエラーが発生しました (LunaChatチャンネルが存在しません。運営に連絡してください。)"));
      return true;
    }

    if (cacheLastUpdated + 3000L < System.currentTimeMillis()) {
      channelMembersMCIDListCache =
          ch.getMembers().stream().map(ChannelMember::getName).collect(Collectors.toList());
      cacheLastUpdated = System.currentTimeMillis();
    }

    if (!channelMembersMCIDListCache.contains(p.getName())) {
      p.sendMessage(
          Chat.f("&cあなたは &e{0} &cチャンネルに参加していません！\n&e/ch join {0} &cを実行して参加してください！", ch.getName()));
      return true;
    }

    ChannelChatMessageData data =
        plugin.getMessageDataFactory().createChannelChatMessageData(p, vcChannelName, message);
    plugin.getPublisher().publishChannelChatMessage(data);
    return true;
  }
}
