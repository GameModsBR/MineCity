package br.com.gamemods.minecity.forge.base;

import br.com.gamemods.minecity.MineCity;
import br.com.gamemods.minecity.MineCityConfig;
import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.Server;
import br.com.gamemods.minecity.api.command.CommandSender;
import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.permission.SimpleFlagHolder;
import br.com.gamemods.minecity.api.shape.PrecisePoint;
import br.com.gamemods.minecity.api.world.*;
import br.com.gamemods.minecity.datasource.api.DataSourceException;
import br.com.gamemods.minecity.forge.base.accessors.ICommander;
import br.com.gamemods.minecity.forge.base.accessors.IMinecraftServer;
import br.com.gamemods.minecity.forge.base.accessors.block.IBlockSnapshot;
import br.com.gamemods.minecity.forge.base.accessors.entity.IEntity;
import br.com.gamemods.minecity.forge.base.accessors.entity.IEntityItem;
import br.com.gamemods.minecity.forge.base.accessors.entity.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.item.IItem;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.accessors.world.IChunk;
import br.com.gamemods.minecity.forge.base.accessors.world.IWorldServer;
import br.com.gamemods.minecity.forge.base.command.ForgeCommandSender;
import br.com.gamemods.minecity.forge.base.command.ForgePlayer;
import br.com.gamemods.minecity.forge.base.command.ForgePlayerSender;
import br.com.gamemods.minecity.forge.base.command.ForgeTransformer;
import br.com.gamemods.minecity.structure.ClaimedChunk;
import br.com.gamemods.minecity.structure.Inconsistency;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.util.BlockSnapshot;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.ToIntBiFunction;
import java.util.stream.Stream;

public class MineCityForge implements Server, ChunkProvider, WorldProvider
{
    private final ConcurrentLinkedQueue<Task> syncTasks = new ConcurrentLinkedQueue<>();
    public Logger logger;
    public IMinecraftServer server;
    private ExecutorService executors;
    private MineCityConfig config;
    private Path worldContainer;
    public ForgeTransformer transformer;
    public MineCity mineCity;
    public Item selectionTool;
    public Consumer<ForgePlayerSender.ForgeSelection> selectionPallet;
    private final Queue<Predicate<IEntity>> entitySpawnListeners = new ConcurrentLinkedQueue<>();

    public void callSyncMethodDelayed(Runnable runnable, int delay)
    {
        syncTasks.add(new Task(runnable, delay));
    }

    public void addSpawnListener(int howLong, Predicate<IEntity> listener)
    {
        entitySpawnListeners.add(listener);
        syncTasks.add(new Task(()-> entitySpawnListeners.remove(listener), howLong));
    }

    public void addPostSpawnListener(Predicate<IEntity> filter, int howLong, Predicate<IEntity> listener)
    {
        AtomicReference<Predicate<IEntity>> reference = new AtomicReference<>();
        Predicate<IEntity> wrapper = entity ->
        {
            if(reference.get() == null)
                return true;

            callSyncMethod(() ->
            {
                if(listener.test(entity))
                    entitySpawnListeners.remove(reference.getAndSet(null));
            });

            return false;
        };

        reference.set(wrapper);
        addSpawnListener(howLong, filter.and(wrapper));
    }

    public <T> void addPostSpawnListener(Class<T> filter, int howLong, Predicate<T> listener)
    {
        addPostSpawnListener(filter::isInstance, howLong, entity -> listener.test(filter.cast(entity)));
    }

    public void addPostSpawnListener(int howLong, Predicate<IEntity> listener)
    {
        addPostSpawnListener(entity -> true, howLong, listener);
    }

    public void addPostSpawnListener(PrecisePoint point, double maxDistance, Predicate<IEntity> filter, int howLong, Predicate<IEntity> listener)
    {
        addPostSpawnListener(filter, howLong, entity ->
            entity.getEntityPos(this).distance(point) <= maxDistance
                    && listener.test(entity)
        );
    }

    public <T> void addPostSpawnListener(PrecisePoint point, double maxDistance, Class<T> filter, int howLong, Predicate<T> listener)
    {
        addPostSpawnListener(point, maxDistance, filter::isInstance, howLong, entity -> listener.test(filter.cast(entity)));
    }

    public void addItemConsumer(PrecisePoint point, double maxDistance, int amount, int howLong, ToIntBiFunction<IEntityItem, Integer> listener)
    {
        AtomicInteger consume = new AtomicInteger(amount);
        addPostSpawnListener(point, maxDistance, IEntityItem.class, howLong, entity ->
        {
            if(consume.get() <= 0)
                return true;

            IItemStack stack = entity.getStack();
            if(stack.getSize() <= 0)
                entity.setDead();
            else
            {
                int remaining =  consume.addAndGet(-listener.applyAsInt(entity, consume.get()));
                if(stack.getSize() <= 0)
                    entity.setDead();
                return remaining <= 0;
            }

            return false;
        });
    }

    public void consumeItems(PrecisePoint point, double maxDistance, int amount, int howLong, @Nullable IItem item, ToIntBiFunction<IEntityItem, Integer> or)
    {
        AtomicInteger consume = new AtomicInteger(amount);
        addItemConsumer(point, maxDistance, Integer.MAX_VALUE, howLong, (entity, r)-> {
            IItemStack stack = entity.getStack();
            int remaining = consume.get();
            if(remaining <= 0 || stack.getIItem() != item)
            {
                consume.addAndGet(or.applyAsInt(entity, remaining));
                return 0;
            }

            int size = stack.getSize();
            int remove = Math.min(size, remaining);
            stack.setSize(size - remove);
            consume.addAndGet(-remove);
            return 0;
        });
    }

    public void consumeItemsOrAddOwnerIf(PrecisePoint point, double maxDistance, int amount, int howLong, @Nullable IItem item, PlayerID owner, Predicate<IEntityItem> cond)
    {
        consumeItems(point, maxDistance, amount, howLong, item, (entity, remaining) -> {
            if(cond.test(entity))
            {
                entity.allowToPickup(owner);
                entity.setItemOwner(owner.getName());
            }
            return 0;
        });
    }

    public void callSpawnListeners(IEntity entity)
    {
        Iterator<Predicate<IEntity>> iter = entitySpawnListeners.iterator();
        while(iter.hasNext())
            if(iter.next().test(entity))
                iter.remove();
    }

    private void adjustDefaultFlag(Configuration config, String prefix, PermissionFlag flag, boolean def, SimpleFlagHolder flags)
    {
        boolean allow = config.getBoolean("allow", prefix+flag, def, "If this permission is allowed by default");
        String msg = config.getString("message", prefix+flag, "",
                "The message that will be displayed when this permission is denied, leave blank for the default message."
        );

        if(!msg.isEmpty())
            flags.getDefaultMessages().put(flag, Message.string(msg));

        if(!allow)
            flags.deny(flag);
    }

    public void onPreInit(Configuration config, Logger logger, ForgeTransformer transformer) throws IOException, SAXException
    {
        this.transformer = transformer;
        this.config = new MineCityConfig();
        this.config.locale = Locale.forLanguageTag(config.get("general", "language", "en").getString());
        this.config.useTitle = config.get("general", "use-titles", true).getBoolean();
        this.config.dbUrl = config.get("database", "url", this.config.dbUrl).getString();
        this.config.dbUser = Optional.of(config.get("database", "user", "").getString())
                .filter(u->!u.isEmpty()).orElse(null);

        this.config.dbPass = Optional.of(config.get("database", "pass", "").getString())
                .filter(p->!p.isEmpty()).map(String::getBytes).orElse(null);

        String defaultMsg = config.getString("default-message", "permissions.default.nature", "",
                "The default message to be displayed when a permission is denied, leave blank for a translatable default message.");

        if(!defaultMsg.isEmpty())
            this.config.defaultNatureFlags.setDefaultMessage(Message.string(defaultMsg));

        defaultMsg = config.getString("default-message", "permissions.default.city", "",
                "The default message to be displayed when a permission is denied, leave blank for a translatable default message.");
        if(!defaultMsg.isEmpty())
            this.config.defaultCityFlags.setDefaultMessage(Message.string(defaultMsg));

        this.config.defaultNatureDisableCities = !config.getBoolean("enable-city-creation", "permissions.default.nature", true,
                "Allow cities to be created in new dimensions");

        for(PermissionFlag flag: PermissionFlag.values())
        {
            adjustDefaultFlag(config, "permissions.default.nature.", flag, flag.defaultNature, this.config.defaultNatureFlags);
            adjustDefaultFlag(config, "permissions.default.city.", flag, flag.defaultCity, this.config.defaultCityFlags);
            adjustDefaultFlag(config, "permissions.default.plot.", flag, flag.defaultPlot, this.config.defaultPlotFlags);
            adjustDefaultFlag(config, "permissions.default.reserve.", flag, flag.defaultReserve, this.config.defaultReserveFlags);
        }

        config.save();

        transformer.parseXML(MineCity.class.getResourceAsStream("/assets/minecity/messages-en.xml"));

        String lang = this.config.locale.toLanguageTag();
        if(!lang.equals("en"))
        {
            try
            {
                InputStream resource = MineCity.class.getResourceAsStream("/assets/minecity/messages-"+lang+".xml");
                if(resource != null)
                {
                    try
                    {
                        transformer.parseXML(resource);
                    }
                    finally
                    {
                        resource.close();
                    }
                }
                else
                {
                    logger.error("There're no translations to "+lang+" available.");
                    this.config.locale = Locale.ENGLISH;
                }
            }
            catch(Exception e)
            {
                logger.error("Failed to load the "+lang+" translations", e);
            }
        }
    }

    public void onServerAboutToStart(MinecraftServer server) throws IOException, SAXException, DataSourceException
    {
        executors = Executors.newCachedThreadPool();
        this.server = (IMinecraftServer) server;


        worldContainer = Paths.get(server.getFolderName());

        MineCityConfig config = this.config;
        if(server instanceof IntegratedServer)
            config = config.clone();

        mineCity = new MineCity(this, config, transformer);
        Inconsistency.getInconsistentCity(mineCity);
        mineCity.worldProvider = Optional.of(this);
        String lang = config.locale.toLanguageTag();
        mineCity.commands.parseXml(MineCity.class.getResourceAsStream("/assets/minecity/commands-"+lang+".xml"));
        mineCity.dataSource.initDB();
    }

    public void onServerStop() throws DataSourceException
    {
        executors.shutdown();
        boolean terminated = false;
        try
        {
            terminated = executors.awaitTermination(5, TimeUnit.SECONDS);
        }
        catch(InterruptedException ignored)
        {}

        if(!terminated)
            executors.shutdownNow();

        mineCity.dataSource.close();
        mineCity = null;
        worldContainer = null;
    }

    public void onServerTick()
    {
        EntityUpdate update = mineCity.entityUpdates.peek();
        if(update != null && update.ticks-- <= 0)
            mineCity.entityUpdates.poll();

        mineCity.reloadQueuedChunk();

        // Using iterator instead of poll() to avoid infinite loop when a task register an other task.
        // This won't happens on iterator because it's weakly consistent.
        Iterator<Task> iterator = syncTasks.iterator();
        while(iterator.hasNext())
        {
            Task task = iterator.next();
            if(task.execute())
                iterator.remove();
        }
    }

    @Override
    public MineCity getMineCity()
    {
        return mineCity;
    }

    @Override
    public void runAsynchronously(Runnable runnable)
    {
        executors.submit(runnable);
    }

    @Override
    public <R> Future<R> callSyncMethod(Callable<R> callable)
    {
        FutureTask<R> future = new FutureTask<>(()->{
            try
            {
                return callable.call();
            }
            catch(Throwable e)
            {
                logger.error("A sync task threw an throwable on execution!", e);
                throw e;
            }
        });

        syncTasks.add(new Task(future, 1));
        return future;
    }

    @Override
    public Optional<PlayerID> getPlayerId(String name)
    {
        for(IEntityPlayerMP player : server.getIPlayerList().getIPlayers())
        {
            String playerName = player.getName();
            if(name.equals(playerName))
                return Optional.of(player.getIdentity());
        }

        return Optional.empty();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Stream<PlayerID> getOnlinePlayers()
    {
        return server.getIPlayerList().getPlayerEntities().stream().map(e-> player(e).identity());
    }

    @Override
    @SuppressWarnings("unchecked")
    public Stream<String> getOnlinePlayerNames()
    {
        return server.getIPlayerList().getIPlayers().stream().map(IEntity::getName);
    }

    public WorldDim world(World world)
    {
        return world((IWorldServer) world);
    }

    public WorldDim world(IWorldServer world)
    {
        WorldDim cached = world.getMineCityWorld();
        if(cached != null)
            return cached;

        Path worldPath = worldContainer.resolve(Optional.ofNullable(world.getFolder()).orElse(""));
        WorldDim worldDim = new WorldDim(world.getDimensionId(), worldPath.toString());
        worldDim.instance = world;

        world.setMineCityWorld(worldDim);

        return worldDim;
    }

    @Nullable
    public WorldServer world(WorldDim world)
    {
        if(world.instance instanceof WorldServer && ((IWorldServer) world.instance).getMineCityWorld() != null)
            return (WorldServer) world.instance;

        WorldServer worldServer = DimensionManager.getWorld(world.dim);
        if(worldServer == null || !world.equals(world(worldServer)))
            return null;

        world.instance = worldServer;
        return worldServer;
    }

    @Nullable
    public WorldDim getWorld(int dim, @NotNull String dir)
    {
        WorldServer worldServer = DimensionManager.getWorld(dim);
        if(!(worldServer instanceof IWorldServer))
            return null;

        WorldDim worldDim = ((IWorldServer)worldServer).getMineCityWorld();
        if(worldDim == null)
            worldDim = world(worldServer);

        if(dir.equals(worldDim.dir))
            return worldDim;

        return null;
    }

    public ChunkPos chunk(Chunk chunk)
    {
        ChunkPos pos = ((IChunk) chunk).getMineCityChunk();
        if(pos != null)
            return pos;

        pos = new ChunkPos(world(chunk.getWorld()), chunk.xPosition, chunk.zPosition);
        pos.instance = chunk;
        return pos;
    }

    @NotNull
    @Override
    public Stream<ClaimedChunk> loadedChunks()
    {
        IWorldServer overWorld = (IWorldServer) DimensionManager.getWorld(0);
        if(overWorld == null)
            return Stream.empty();

        Collection<IChunk> overWorldChunks = overWorld.getLoadedIChunks();
        if(overWorldChunks.isEmpty())
            return Stream.empty();

        Stream<IChunk> composite = overWorldChunks.stream();
        for(WorldServer worldServer : DimensionManager.getWorlds())
        {
            if(worldServer == overWorld) continue;
            composite = Stream.concat(composite, ((IWorldServer) worldServer).getLoadedIChunks().stream());
        }

        return composite.map(IChunk::getMineCityClaim);
    }

    public IChunk getLoadedChunk(WorldServer world, int x, int z)
    {
        return ((IWorldServer) world).getLoadedChunk(x, z);
    }

    protected
        ForgePlayer <MineCityForge, IEntityPlayerMP, ForgePlayerSender<IEntityPlayerMP, MineCityForge>>
            createPlayer(IEntityPlayerMP player)
    {
        return new ForgePlayer<>(new ForgePlayerSender<>(this, player));
    }

    protected ForgeCommandSender<ICommander, MineCityForge> createSender(ICommander sender)
    {
        return new ForgeCommandSender<>(this, sender);
    }

    public ForgePlayer player(EntityPlayer player)
    {
        return player((IEntityPlayerMP) player);
    }

    public ForgePlayer player(IEntityPlayerMP player)
    {
        ForgePlayer cache = player.getMineCityPlayer();
        if(cache != null)
            return cache;

        cache = createPlayer(player);
        player.setMineCityPlayer(cache);
        return cache;
    }

    public CommandSender sender(ICommandSender sender)
    {
        if(sender instanceof EntityPlayer)
            return player((EntityPlayer) sender).getCommandSender();
        return createSender((ICommander) sender);
    }

    @Nullable
    public ClaimedChunk getClaim(@NotNull WorldDim dim, int x, int z)
    {
        WorldServer world = world(dim);
        if(world == null)
            return null;

        IChunk chunk = getLoadedChunk(world, x, z);
        if(chunk == null)
            return null;

        return chunk.getMineCityClaim();
    }

    @Nullable
    public ClaimedChunk getClaim(@NotNull ChunkPos pos)
    {
        IChunk chunk = (IChunk) pos.instance;
        if(chunk != null && chunk.getForgeWorldServer().getMineCityWorld() != null)
        {
            ClaimedChunk claim = chunk.getMineCityClaim();
            if(claim != null)
                return claim;
        }

        WorldServer world = world(pos.world);
        if(world == null)
            return null;

        chunk = getLoadedChunk(world, pos.x, pos.z);
        if(chunk == null)
            return null;

        pos.instance = chunk;
        return chunk.getMineCityClaim();
    }

    public boolean setClaim(@NotNull ClaimedChunk claim)
    {
        ChunkPos pos = claim.chunk;
        if(pos.instance instanceof IChunk)
        {
            IChunk chunk = (IChunk) pos.instance;
            if(chunk.isMineCityChunkValid())
            {
                ClaimedChunk old = chunk.getMineCityClaim();
                chunk.setMineCityClaim(claim);
                if(old != null && old != claim)
                    old.invalidate();
                return true;
            }
        }

        WorldServer worldServer = world(pos.world);
        if(!(worldServer instanceof IWorldServer))
            return false;

        IChunk forgeChunk = getLoadedChunk(worldServer, pos.x, pos.z);
        if(forgeChunk == null)
            return false;

        pos.instance = forgeChunk;
        forgeChunk.setMineCityClaim(claim);
        return true;
    }

    @Nullable
    @Override
    public Optional<ChunkProvider> getChunkProvider()
    {
        return Optional.of(this);
    }

    @Nullable
    public IChunk chunk(ChunkPos pos)
    {
        return getLoadedChunk(world(pos.world), pos.x, pos.z);
    }

    @Nullable
    @Override
    public ChunkPos getChunk(@NotNull WorldDim dim, int x, int z)
    {
        WorldServer world = world(dim);
        if(world == null)
            return null;

        IChunk chunk = getLoadedChunk(world, x, z);
        if(chunk == null)
            return null;

        ChunkPos pos = chunk.getMineCityChunk();
        if(pos != null)
            return pos;

        pos = new ChunkPos(dim, x, z);
        pos.instance = chunk;
        return pos;
    }

    @Deprecated
    public boolean isTopSolid(World world, int x, int y, int z)
    {
        return ((IWorldServer) world).isTopSolid(x, y, z);
    }

    @Deprecated
    public Entity vehicle(Entity entity)
    {
        return ((IEntity) entity).getVehicle();
    }

    @Deprecated
    public br.com.gamemods.minecity.api.world.BlockPos block(Entity entity)
    {
        return ((IEntity) entity).getBlockPos(this);
    }

    public BlockPos block(BlockSnapshot snapshot)
    {
        return ((IBlockSnapshot) snapshot).getPosition(this);
    }

    public IItemStack stack(ItemStack stack)
    {
        return (IItemStack) (Object) stack;
    }
}
