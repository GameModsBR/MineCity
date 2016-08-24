package br.com.gamemods.minecity.forge.base;

import br.com.gamemods.minecity.MineCity;
import br.com.gamemods.minecity.MineCityConfig;
import br.com.gamemods.minecity.api.Server;
import br.com.gamemods.minecity.api.command.CommandSender;
import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.command.MessageTransformer;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.permission.SimpleFlagHolder;
import br.com.gamemods.minecity.api.world.*;
import br.com.gamemods.minecity.datasource.api.DataSourceException;
import br.com.gamemods.minecity.forge.base.accessors.IChunk;
import br.com.gamemods.minecity.forge.base.accessors.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.IWorldServer;
import br.com.gamemods.minecity.forge.base.command.IForgePlayer;
import br.com.gamemods.minecity.structure.ClaimedChunk;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.config.Configuration;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.*;

public abstract class MineCityForge implements Server, ChunkProvider, WorldProvider
{
    private final ConcurrentLinkedQueue<FutureTask> syncTasks = new ConcurrentLinkedQueue<>();
    public Logger logger;
    public MinecraftServer server;
    private ExecutorService executors;
    private MineCityConfig config;
    private Path worldContainer;
    public MessageTransformer transformer;
    public MineCity mineCity;

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

    public void onPreInit(Configuration config, Logger logger, MessageTransformer transformer) throws IOException, SAXException
    {
        this.transformer = transformer;
        this.config = new MineCityConfig();
        this.config.locale = Locale.forLanguageTag(config.get("general", "language", "en").getString());
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
        this.server = server;


        worldContainer = Paths.get(server.getFolderName());

        mineCity = new MineCity(this, config, transformer);
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
        Iterator<FutureTask> iterator = syncTasks.iterator();
        while(iterator.hasNext())
        {
            FutureTask task = iterator.next();
            iterator.remove();
            task.run();
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
        FutureTask<R> future = new FutureTask<>(callable);
        syncTasks.add(future);
        return future;
    }

    public int dimension(World world)
    {
        return world.provider.getDimension();
    }

    public WorldDim world(World world)
    {
        WorldDim cached = ((IWorldServer) world).getMineCityWorld();
        if(cached != null)
            return cached;

        Path worldPath = worldContainer.resolve(Optional.ofNullable(world.provider.getSaveFolder()).orElse(""));
        WorldDim worldDim = new WorldDim(dimension(world), worldPath.toString());
        worldDim.instance = world;

        ((IWorldServer) world).setMineCityWorld(worldDim);

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

    public abstract Chunk getLoadedChunk(WorldServer world, int x, int z);

    protected abstract IForgePlayer createPlayer(EntityPlayerMP player);
    protected abstract CommandSender createSender(ICommandSender sender);

    public IForgePlayer player(EntityPlayer player)
    {
        IEntityPlayerMP cast = ((IEntityPlayerMP) player);
        IForgePlayer cache = cast.getMineCityPlayer();
        if(cache != null)
            return cache;

        cache = createPlayer((EntityPlayerMP) player);
        cast.setMineCityPlayer(cache);
        return cache;
    }

    public CommandSender sender(ICommandSender sender)
    {
        if(sender instanceof EntityPlayer)
            return player((EntityPlayer) sender).getCommandSender();
        return createSender(sender);
    }

    @Nullable
    public ClaimedChunk getClaim(@NotNull WorldDim dim, int x, int z)
    {
        WorldServer world = world(dim);
        if(world == null)
            return null;

        Chunk chunk = getLoadedChunk(world, x, z);
        if(!(chunk instanceof IChunk))
            return null;

        return ((IChunk) chunk).getMineCityClaim();
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

        chunk = (IChunk) getLoadedChunk(world, pos.x, pos.z);
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

        Chunk forgeChunk = getLoadedChunk(worldServer, pos.x, pos.z);
        if(!(forgeChunk instanceof IChunk))
            return false;

        pos.instance = forgeChunk;
        ((IChunk) forgeChunk).setMineCityClaim(claim);
        return true;
    }

    @Nullable
    @Override
    public Optional<ChunkProvider> getChunkProvider()
    {
        return Optional.of(this);
    }

    @Nullable
    public Chunk chunk(ChunkPos pos)
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

        Chunk chunk = getLoadedChunk(world, x, z);
        if(!(chunk instanceof IChunk))
            return null;

        ChunkPos pos = ((IChunk) chunk).getMineCityChunk();
        if(pos != null)
            return pos;

        pos = new ChunkPos(dim, x, z);
        pos.instance = chunk;
        return pos;
    }
}
