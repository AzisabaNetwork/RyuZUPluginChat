package net.azisaba.ryuzupluginchat;

import co.aikar.taskchain.TaskChain;
import co.aikar.taskchain.TaskChainFactory;
import java.io.File;
import java.util.Random;
import lombok.Getter;
import net.azisaba.ryuzupluginchat.command.HideCommand;
import net.azisaba.ryuzupluginchat.command.RPCCommand;
import net.azisaba.ryuzupluginchat.command.ReplyCommand;
import net.azisaba.ryuzupluginchat.command.TellCommand;
import net.azisaba.ryuzupluginchat.command.UnHideCommand;
import net.azisaba.ryuzupluginchat.command.VCCommand;
import net.azisaba.ryuzupluginchat.config.RPCConfig;
import net.azisaba.ryuzupluginchat.discord.DiscordHandler;
import net.azisaba.ryuzupluginchat.discord.DiscordMessageConnection;
import net.azisaba.ryuzupluginchat.listener.ChatListener;
import net.azisaba.ryuzupluginchat.listener.JoinQuitListener;
import net.azisaba.ryuzupluginchat.listener.LunaChatHideCommandListener;
import net.azisaba.ryuzupluginchat.listener.OutdatedCommandCaptureListener;
import net.azisaba.ryuzupluginchat.message.JsonDataConverter;
import net.azisaba.ryuzupluginchat.message.MessageDataFactory;
import net.azisaba.ryuzupluginchat.message.MessageProcessor;
import net.azisaba.ryuzupluginchat.message.PrivateChatInspectHandler;
import net.azisaba.ryuzupluginchat.redis.HideInfoController;
import net.azisaba.ryuzupluginchat.redis.MessagePublisher;
import net.azisaba.ryuzupluginchat.redis.MessageSubscriber;
import net.azisaba.ryuzupluginchat.redis.PlayerUUIDMapContainer;
import net.azisaba.ryuzupluginchat.redis.PrivateChatIDGetter;
import net.azisaba.ryuzupluginchat.redis.PrivateChatReachedSubscriber;
import net.azisaba.ryuzupluginchat.redis.PrivateChatResponseWaiter;
import net.azisaba.ryuzupluginchat.redis.ReplyTargetFetcher;
import net.azisaba.ryuzupluginchat.redis.RyuZUPrefixSuffixContainer;
import net.azisaba.ryuzupluginchat.redis.VCLunaChatChannelSharer;
import net.azisaba.ryuzupluginchat.task.SubscriberPingTask;
import net.azisaba.ryuzupluginchat.taskchain.BukkitTaskChainFactory;
import net.azisaba.ryuzupluginchat.updater.GitHubPluginUpdater;
import net.azisaba.ryuzupluginchat.updater.UpdateStatus;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

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
  private final PrivateChatInspectHandler privateChatInspectHandler =
      new PrivateChatInspectHandler();

  private MessagePublisher publisher;
  private MessageSubscriber subscriber;
  private PrivateChatReachedSubscriber privateChatReachedSubscriber;

  private DiscordHandler discordHandler;

  private JedisPool jedisPool;

  private GitHubPluginUpdater updater;

  @Override
  public void onEnable() {
    taskChainFactory = BukkitTaskChainFactory.create(this);

    rpcConfig = new RPCConfig(this);
    rpcConfig.load();

    messageDataFactory = new MessageDataFactory(this);
    messageProcessor = new MessageProcessor(this);
    jsonDataConverter = new JsonDataConverter(this);
    privateChatResponseWaiter = new PrivateChatResponseWaiter(this);

    privateChatResponseWaiter.runTimeoutDetectTask(this);

    setupRedisConnections();

    getServer().getPluginManager().registerEvents(new ChatListener(this), this);
    getServer().getPluginManager().registerEvents(new JoinQuitListener(this), this);
    getServer().getPluginManager().registerEvents(new OutdatedCommandCaptureListener(this), this);
    getServer().getPluginManager().registerEvents(new LunaChatHideCommandListener(this), this);

    registerCommands();

    if (rpcConfig.isDiscordBotEnabled()) {
      setupDiscordConnection();
    }

    new SubscriberPingTask(this, subscriber, privateChatReachedSubscriber).run();

    // 6 hours
    int randomTicks = 20 * 60 * 60 * 6;
    Bukkit.getScheduler()
        .runTaskLaterAsynchronously(
            this, this::executeUpdateAsync, new Random().nextInt(randomTicks));

    getLogger().info(getName() + " enabled.");
  }

  @Override
  public void onDisable() {
    subscriber.getExecutorService().shutdownNow();
    privateChatReachedSubscriber.getExecutorService().shutdownNow();
    subscriber.unregisterAll();
    if (discordHandler != null) {
      discordHandler.disconnect();
    }

    Bukkit.getLogger().info(getName() + " disabled.");
  }

  public void executeFullReload() {
    /** Shutdown Processes */
    // Redis Subscribers
    subscriber.getExecutorService().shutdownNow();
    subscriber.unregisterAll();
    privateChatReachedSubscriber.getExecutorService().shutdownNow();

    // Discord
    if (discordHandler != null) {
      discordHandler.disconnect();
    }

    /** Startup Processes */
    // Redis
    setupRedisConnections();

    // Discord
    if (rpcConfig.isDiscordBotEnabled()) {
      setupDiscordConnection();
    }
  }

  private void setupRedisConnections() {
    jedisPool =
        new JedisPool(
            new JedisPoolConfig(),
            rpcConfig.getHostAndPort().getHost(),
            rpcConfig.getHostAndPort().getPort(),
            3000, // timeout
            rpcConfig.getRedisPassword());

    publisher = new MessagePublisher(this, jedisPool, jsonDataConverter, rpcConfig.getGroupName());

    subscriber =
        new MessageSubscriber(this, jsonDataConverter, jedisPool, rpcConfig.getGroupName());
    subscriber.subscribe();

    // Same as above
    privateChatReachedSubscriber =
        new PrivateChatReachedSubscriber(this, jedisPool, rpcConfig.getGroupName());
    privateChatReachedSubscriber.subscribe();

    subscriber.registerFunctions();

    prefixSuffixContainer = new RyuZUPrefixSuffixContainer(jedisPool, rpcConfig.getGroupName());
    playerUUIDMapContainer = new PlayerUUIDMapContainer(this, jedisPool, rpcConfig.getGroupName());
    replyTargetFetcher = new ReplyTargetFetcher(jedisPool, rpcConfig.getGroupName());
    privateChatIDGetter = new PrivateChatIDGetter(jedisPool, rpcConfig.getGroupName());
    vcLunaChatChannelSharer = new VCLunaChatChannelSharer(jedisPool, rpcConfig.getGroupName());
    hideInfoController = new HideInfoController(jedisPool, rpcConfig.getGroupName());
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

  private void executeUpdateAsync() {
    updater = new GitHubPluginUpdater(this, getDescription().getVersion());
    RyuZUPluginChat.newChain()
        .asyncFirst(
            () -> {
              updater.checkUpdate();
              return updater.getStatus();
            })
        .abortIf((status) -> status != UpdateStatus.OUTDATED)
        .async(
            () -> {
              File file = new File(Bukkit.getUpdateFolderFile(), getName() + ".jar");

              if (!Bukkit.getUpdateFolderFile().exists()) {
                if (!Bukkit.getUpdateFolderFile().mkdirs()) {
                  getLogger()
                      .warning("Failed to create dir " + Bukkit.getUpdateFolderFile().getPath());
                  return;
                }
              }
              boolean success = updater.executeDownloadLatestJar(file);

              if (success) {
                getLogger().info("Newer version installed. It will be applied after restart.");
              } else {
                getLogger().warning("Failed to install newer version.");
              }
            })
        .execute();
  }

  public static <T> TaskChain<T> newChain() {
    return taskChainFactory.newChain();
  }

  public static <T> TaskChain<T> newSharedChain(String name) {
    return taskChainFactory.newSharedChain(name);
  }
}
