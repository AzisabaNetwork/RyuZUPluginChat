package ryuzupluginchat.ryuzupluginchat;

import co.aikar.taskchain.BukkitTaskChainFactory;
import co.aikar.taskchain.TaskChain;
import co.aikar.taskchain.TaskChainFactory;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;
import redis.clients.jedis.Jedis;
import ryuzupluginchat.ryuzupluginchat.command.HideCommand;
import ryuzupluginchat.ryuzupluginchat.command.RPCCommand;
import ryuzupluginchat.ryuzupluginchat.command.ReplyCommand;
import ryuzupluginchat.ryuzupluginchat.command.TellCommand;
import ryuzupluginchat.ryuzupluginchat.command.UnHideCommand;
import ryuzupluginchat.ryuzupluginchat.command.VCCommand;
import ryuzupluginchat.ryuzupluginchat.config.RPCConfig;
import ryuzupluginchat.ryuzupluginchat.discord.DiscordHandler;
import ryuzupluginchat.ryuzupluginchat.discord.DiscordMessageConnection;
import ryuzupluginchat.ryuzupluginchat.listener.ChatListener;
import ryuzupluginchat.ryuzupluginchat.listener.JoinQuitListener;
import ryuzupluginchat.ryuzupluginchat.listener.OutdatedCommandCaptureListener;
import ryuzupluginchat.ryuzupluginchat.listener.LunaChatHideCommandListener;
import ryuzupluginchat.ryuzupluginchat.message.JsonDataConverter;
import ryuzupluginchat.ryuzupluginchat.message.MessageDataFactory;
import ryuzupluginchat.ryuzupluginchat.message.MessageProcessor;
import ryuzupluginchat.ryuzupluginchat.redis.HideInfoController;
import ryuzupluginchat.ryuzupluginchat.redis.MessagePublisher;
import ryuzupluginchat.ryuzupluginchat.redis.MessageSubscriber;
import ryuzupluginchat.ryuzupluginchat.redis.PlayerUUIDMapContainer;
import ryuzupluginchat.ryuzupluginchat.redis.PrivateChatIDGetter;
import ryuzupluginchat.ryuzupluginchat.redis.PrivateChatReachedSubscriber;
import ryuzupluginchat.ryuzupluginchat.redis.PrivateChatResponseWaiter;
import ryuzupluginchat.ryuzupluginchat.redis.ReplyTargetFetcher;
import ryuzupluginchat.ryuzupluginchat.redis.RyuZUPrefixSuffixContainer;
import ryuzupluginchat.ryuzupluginchat.redis.VCLunaChatChannelSharer;

@Getter
public final class RyuZUPluginChat extends JavaPlugin {

  private static TaskChainFactory taskChainFactory;

  private RPCConfig rpcConfig;
  private MessageDataFactory messageDataFactory;
  private MessageProcessor messageProcessor;
  private RyuZUPrefixSuffixContainer prefixSuffixContainer;
  private PlayerUUIDMapContainer playerUUIDMapContainer;
  private ReplyTargetFetcher replyTargetFetcher;
  private JsonDataConverter jsonDataConverter;
  private PrivateChatIDGetter privateChatIDGetter;
  private VCLunaChatChannelSharer vcLunaChatChannelSharer;
  private PrivateChatResponseWaiter privateChatResponseWaiter;
  private HideInfoController hideInfoController;

  private MessagePublisher publisher;
  private MessageSubscriber subscriber;
  private PrivateChatReachedSubscriber privateChatReachedSubscriber;

  private DiscordHandler discordHandler;

  @Override

  public void onEnable() {
    taskChainFactory = BukkitTaskChainFactory.create(this);

    rpcConfig = new RPCConfig(this);
    rpcConfig.load();

    messageDataFactory = new MessageDataFactory(this);
    messageProcessor = new MessageProcessor(this);
    jsonDataConverter = new JsonDataConverter(this);
    privateChatResponseWaiter = new PrivateChatResponseWaiter(this);

    setupRedisConnections();

    getServer().getPluginManager().registerEvents(new ChatListener(this), this);
    getServer().getPluginManager().registerEvents(new JoinQuitListener(this), this);
    getServer().getPluginManager().registerEvents(new OutdatedCommandCaptureListener(this), this);
    getServer().getPluginManager().registerEvents(new LunaChatHideCommandListener(this), this);

    registerCommands();

    if (rpcConfig.isDiscordBotEnabled()) {
      setupDiscordConnection();
    }

    getLogger().info(getName() + " enabled.");
  }

  @Override
  public void onDisable() {
    if (discordHandler != null) {
      discordHandler.disconnect();
    }

    Bukkit.getLogger().info(getName() + " disabled.");
  }

  private void setupRedisConnections() {
    Jedis jedis = getConnectedJedis();

    publisher = new MessagePublisher(jedis, jsonDataConverter, rpcConfig.getGroupName());

    // create new jedis instance because subscribe will block other actions like hset etc.
    subscriber = new MessageSubscriber(this, jsonDataConverter, getConnectedJedis(),
        rpcConfig.getGroupName());
    subscriber.subscribe();

    // Same as above
    privateChatReachedSubscriber = new PrivateChatReachedSubscriber(this, getConnectedJedis(),
        rpcConfig.getGroupName());
    privateChatReachedSubscriber.subscribe();

    subscriber.registerFunctions();

    prefixSuffixContainer = new RyuZUPrefixSuffixContainer(jedis,
        rpcConfig.getGroupName());
    playerUUIDMapContainer = new PlayerUUIDMapContainer(this, jedis, rpcConfig.getGroupName());
    replyTargetFetcher = new ReplyTargetFetcher(jedis, rpcConfig.getGroupName());
    privateChatIDGetter = new PrivateChatIDGetter(jedis, rpcConfig.getGroupName());
    vcLunaChatChannelSharer = new VCLunaChatChannelSharer(jedis, rpcConfig.getGroupName());
    hideInfoController = new HideInfoController(jedis, rpcConfig.getGroupName());
  }

  private void setupDiscordConnection() {
    vcLunaChatChannelSharer.setLunaChatChannelName(rpcConfig.getVcCommandLunaChatChannel());
    discordHandler = new DiscordHandler(this, rpcConfig.getDiscordBotToken());
    boolean initResult = discordHandler.init();

    if (!initResult) {
      getLogger().warning("Failed to login to Discord Bot. Is that the correct Token?");
      return;
    }

    for (DiscordMessageConnection connectionData : rpcConfig.getMessageConnections()) {
      discordHandler.connectUsing(connectionData);
    }
  }

  private Jedis getConnectedJedis() {
    Jedis jedis = new Jedis(rpcConfig.getHostAndPort());
    jedis.auth(
//        rpcConfig.getRedisUserName(),
        rpcConfig.getRedisPassword());
    return jedis;
  }

  private void registerCommands() {
    registerCommand("rpc", new RPCCommand(this));
    registerCommand("tell", new TellCommand(this));
    registerCommand("reply", new ReplyCommand(this));
    registerCommand("vc", new VCCommand(this, vcLunaChatChannelSharer));
    registerCommand("hide", new HideCommand(this));
    registerCommand("unhide", new UnHideCommand(this));
  }

  private void registerCommand(String commandName, CommandExecutor executor) {
    PluginCommand cmd = getCommand(commandName);
    if (cmd == null) {
      getLogger().warning("Failed to register command named " + commandName);
      return;
    }

    cmd.setExecutor(executor);
    if (executor instanceof TabCompleter) {
      cmd.setTabCompleter((TabCompleter) executor);
    }
  }

  public static <T> TaskChain<T> newChain() {
    return taskChainFactory.newChain();
  }

  public static <T> TaskChain<T> newSharedChain(String name) {
    return taskChainFactory.newSharedChain(name);
  }
}
