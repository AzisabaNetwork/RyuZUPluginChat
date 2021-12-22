package ryuzupluginchat.ryuzupluginchat;

import co.aikar.taskchain.BukkitTaskChainFactory;
import co.aikar.taskchain.TaskChain;
import co.aikar.taskchain.TaskChainFactory;
import java.util.Objects;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import redis.clients.jedis.Jedis;
import ryuzupluginchat.ryuzupluginchat.command.RPCCommand;
import ryuzupluginchat.ryuzupluginchat.command.ReplyCommand;
import ryuzupluginchat.ryuzupluginchat.command.TellCommand;
import ryuzupluginchat.ryuzupluginchat.config.RPCConfig;
import ryuzupluginchat.ryuzupluginchat.listener.ChatListener;
import ryuzupluginchat.ryuzupluginchat.listener.JoinListener;
import ryuzupluginchat.ryuzupluginchat.redis.MessagePublisher;
import ryuzupluginchat.ryuzupluginchat.redis.MessageSubscriber;
import ryuzupluginchat.ryuzupluginchat.redis.ReplyMessageCacheContainer;
import ryuzupluginchat.ryuzupluginchat.redis.RyuZUPrefixSuffixContainer;
import ryuzupluginchat.ryuzupluginchat.util.MessageDataFactory;
import ryuzupluginchat.ryuzupluginchat.util.PlayerUUIDMapContainer;

@Getter
public final class RyuZUPluginChat extends JavaPlugin {

  private static TaskChainFactory taskChainFactory;

  private RPCConfig rpcConfig;
  private MessageDataFactory messageDataFactory;
  private MessageProcessor messageProcessor;
  private RyuZUPrefixSuffixContainer prefixSuffixContainer;
  private PlayerUUIDMapContainer playerUUIDMapContainer;
  private final ReplyMessageCacheContainer replyTargetContainer = new ReplyMessageCacheContainer();

  private MessagePublisher publisher;
  private MessageSubscriber subscriber;


  @Override
  public void onEnable() {
    taskChainFactory = BukkitTaskChainFactory.create(this);

    rpcConfig = new RPCConfig(this);
    rpcConfig.load();

    messageDataFactory = new MessageDataFactory(this);
    messageProcessor = new MessageProcessor(this);

    setupRedisConnections();

    getServer().getPluginManager().registerEvents(new ChatListener(this), this);
    getServer().getPluginManager().registerEvents(new JoinListener(this), this);

    registerCommands();

    getLogger().info(getName() + " enabled.");
  }

  @Override
  public void onDisable() {
    Bukkit.getLogger().info(getName() + " disabled.");
  }

  private void setupRedisConnections() {
    Jedis jedis = new Jedis(rpcConfig.getHostAndPort());
    jedis.auth(rpcConfig.getRedisUserName(), rpcConfig.getRedisPassword());

    publisher = new MessagePublisher(jedis, rpcConfig.getGlobalChannel(),
        rpcConfig.getPrivateChannel(), rpcConfig.getChannelChatChannel(),
        rpcConfig.getSystemChannel());

    subscriber = new MessageSubscriber(this, jedis, rpcConfig.getGlobalChannel(),
        rpcConfig.getPrivateChannel(), rpcConfig.getChannelChatChannel(),
        rpcConfig.getSystemChannel());
    subscriber.subscribe();

    subscriber.registerFunctions();

    prefixSuffixContainer = new RyuZUPrefixSuffixContainer(jedis, rpcConfig.getPrefixMapKey(),
        rpcConfig.getSuffixMapKey());

    playerUUIDMapContainer = new PlayerUUIDMapContainer(this, jedis,
        rpcConfig.getUuidMapKey());
  }

  private void registerCommands() {
    RPCCommand rpcCommand = new RPCCommand(this);
    TellCommand tellCommand = new TellCommand(this);
    ReplyCommand replyCommand = new ReplyCommand(this);

    Objects.requireNonNull(getCommand("rpc")).setExecutor(rpcCommand);
    Objects.requireNonNull(getCommand("rpc")).setTabCompleter(rpcCommand);
    Objects.requireNonNull(getCommand("tell")).setExecutor(tellCommand);
    Objects.requireNonNull(getCommand("tell")).setTabCompleter(tellCommand);
    Objects.requireNonNull(getCommand("reply")).setExecutor(replyCommand);
  }

  public static <T> TaskChain<T> newChain() {
    return taskChainFactory.newChain();
  }

  public static <T> TaskChain<T> newSharedChain(String name) {
    return taskChainFactory.newSharedChain(name);
  }
}
