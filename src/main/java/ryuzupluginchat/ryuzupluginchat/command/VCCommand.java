package ryuzupluginchat.ryuzupluginchat.command;

import com.github.ucchyocean.lc3.LunaChat;
import com.github.ucchyocean.lc3.channel.Channel;
import com.github.ucchyocean.lc3.member.ChannelMember;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ryuzupluginchat.ryuzupluginchat.RyuZUPluginChat;
import ryuzupluginchat.ryuzupluginchat.message.data.ChannelChatMessageData;
import ryuzupluginchat.ryuzupluginchat.redis.VCLunaChatChannelSharer;

@RequiredArgsConstructor
public class VCCommand implements CommandExecutor {

  private final RyuZUPluginChat plugin;
  private final VCLunaChatChannelSharer vcChannelGetter;

  private List<String> channelMembersMCIDListCache;
  private long cacheLastUpdated = 0L;

  private HashMap<UUID, Long> lastExecuted = new HashMap<>();

  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd,
      @NotNull String label, @NotNull String[] args) {

    if (!(sender instanceof Player)) {
      sender.sendMessage(ChatColor.RED + "このコマンドはプレイヤーのみ実行可能です");
      return true;
    }

    if (args.length <= 0) {
      sender.sendMessage(ChatColor.RED + "使い方: /" + label + " <メッセージ>");
      return true;
    }

    Player p = (Player) sender;
    String message = String.join(" ", args);
    String vcChannelName = vcChannelGetter.getLunaChatChannelName();

    if (lastExecuted.getOrDefault(p.getUniqueId(), 0L) + 1000L > System.currentTimeMillis()) {
      p.sendMessage(ChatColor.RED + "クールタイム中です。1秒間待って実行してください。");
      return true;
    }
    lastExecuted.put(p.getUniqueId(), System.currentTimeMillis());

    if (vcChannelName == null) {
      p.sendMessage(ChatColor.RED + "エラーが発生しました (Discordと連携するLunaChatチャンネルが不明です)");
      return true;
    }

    Channel ch = LunaChat.getAPI().getChannel(vcChannelName);
    if (ch == null) {
      p.sendMessage(ChatColor.RED + "エラーが発生しました (LunaChatチャンネルが存在しません。運営に連絡してください。)");
      return true;
    }

    if (cacheLastUpdated + 3000L < System.currentTimeMillis()) {
      channelMembersMCIDListCache = ch.getMembers().stream()
          .map(ChannelMember::getName)
          .collect(Collectors.toList());
      cacheLastUpdated = System.currentTimeMillis();
    }

    if (!channelMembersMCIDListCache.contains(p.getName())) {
      p.sendMessage(ChatColor.RED + "あなたは " + ChatColor.YELLOW + ch.getName() + ChatColor.RED
          + " チャンネルに参加していません！\n" + ChatColor.YELLOW + "/ch join " + ch.getName() + ChatColor.RED
          + " を実行して参加してください！");
      return true;
    }
    ChannelChatMessageData data = plugin.getMessageDataFactory()
        .createChannelChatMessageData(p, vcChannelName, message);
    plugin.getPublisher().publishChannelChatMessage(data);
    return true;
  }
}
