package br.com.gamemods.minecity;

import br.com.gamemods.minecity.api.DistinctQueue;
import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.Server;
import br.com.gamemods.minecity.api.command.CommandTree;
import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.command.MessageTransformer;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.*;
import br.com.gamemods.minecity.commands.CityCommand;
import br.com.gamemods.minecity.datasource.api.DataSourceException;
import br.com.gamemods.minecity.datasource.api.IDataSource;
import br.com.gamemods.minecity.datasource.api.unchecked.DBConsumer;
import br.com.gamemods.minecity.datasource.api.unchecked.DBSupplier;
import br.com.gamemods.minecity.datasource.api.unchecked.DisDBConsumer;
import br.com.gamemods.minecity.datasource.api.unchecked.UncheckedDataSourceException;
import br.com.gamemods.minecity.datasource.sql.SQLSource;
import br.com.gamemods.minecity.structure.ClaimedChunk;
import br.com.gamemods.minecity.structure.Inconsistency;
import br.com.gamemods.minecity.structure.Nature;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class MineCity
{
    @NotNull
    public final IDataSource dataSource;
    public final Server server;
    public final CommandTree commands = new CommandTree();
    private final ConcurrentHashMap<WorldDim, Nature> natures = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<ChunkPos, ClaimedChunk> chunks = new ConcurrentHashMap<>();
    public final ConcurrentHashMap<ChunkPos, CityCommand.MapCache> mapCache = new ConcurrentHashMap<>();
    public ExecutorService mapService = Executors.newSingleThreadExecutor(r-> new Thread(r, "MineCityMapService"));
    public Optional<WorldProvider> worldProvider = Optional.empty();
    public MessageTransformer messageTransformer;
    public EnumMap<PermissionFlag, Message> defaultNatureFlags = new EnumMap<>(PermissionFlag.class);
    public EnumMap<PermissionFlag, Message> defaultCityFlags = new EnumMap<>(PermissionFlag.class);
    private Queue<ChunkPos> reloadQueue = new DistinctQueue<>();
    public boolean lazyReloads = true;

    public MineCity(@NotNull Server server, @NotNull MineCityConfig config, @Nullable IDataSource dataSource,
                    @NotNull MessageTransformer messageTransformer)
    {
        this.server = server;
        this.messageTransformer = messageTransformer;
        this.dataSource = dataSource == null? new SQLSource(this, config) : dataSource;
        if(config.dbPass != null)
            Arrays.fill(config.dbPass, (byte) 0);

        commands.cityNames = this.dataSource.cityNameSupplier();
        commands.registerCommands(new CityCommand(this));
        Inconsistency.setMineCity(this);
    }

    public MineCity(Server server, MineCityConfig config)
    {
        this(server, config, null, new MessageTransformer());
    }

    public MineCity(Server server, MineCityConfig config, MessageTransformer messageTransformer)
    {
        this(server, config, null, messageTransformer);
    }

    @NotNull
    public Optional<ClaimedChunk> getChunk(@NotNull BlockPos pos)
    {
        return getChunk(pos.getChunk());
    }

    @NotNull
    public Optional<ClaimedChunk> getChunk(@NotNull ChunkPos pos)
    {
        return Optional.ofNullable(getChunkProvider().map(p-> p.getClaim(pos)).orElseGet(()-> chunks.get(pos)));
    }

    @NotNull
    public ChunkPos provideChunk(@NotNull WorldDim world, int x, int z)
    {
        return getChunkProvider().map(p-> p.getChunk(world, x, z)).orElseGet(()-> new ChunkPos(world, x, z));
    }

    /**
     * @deprecated It's impossible to know if the optional is empty because the chunk is unloaded or because
     * the chunk is not claimed
     */
    @Deprecated
    public Optional<ClaimedChunk> getOrFetchChunk(@NotNull ChunkPos pos) throws DataSourceException
    {
        try
        {
            return Optional.ofNullable(getChunk(pos).orElseGet((DBSupplier<ClaimedChunk>) () -> dataSource.getCityChunk(pos)));
        }
        catch(UncheckedDataSourceException e)
        {
            throw e.getCause();
        }
    }

    @Nullable
    public Nature getNature(@NotNull WorldDim world)
    {
        if(world.nature != null && world.nature.isValid())
            return world.nature;

        return natures.get(world);
    }

    @NotNull
    public Nature loadNature(@NotNull WorldDim world)
    {
        Nature nature = natures.get(world);
        if(nature != null)
            nature.invalidate();

        nature = new Nature(this, world);
        world.nature = nature;
        natures.put(world, nature);
        return nature;
    }

    @NotNull
    public Nature nature(@NotNull WorldDim world)
    {
        Nature nature = getNature(world);
        if(nature != null && nature.isValid()) return nature;
        return loadNature(world);
    }

    public Optional<ChunkProvider> getChunkProvider()
    {
        return worldProvider.flatMap(WorldProvider::getChunkProvider);
    }

    @NotNull
    public ClaimedChunk loadChunk(@NotNull ChunkPos pos) throws DataSourceException
    {
        ClaimedChunk chunk = Optional.ofNullable(dataSource.getCityChunk(pos))
                .orElseGet(()-> new ClaimedChunk(nature(pos.world), pos));

        if(!getChunkProvider().map(p-> p.setClaim(chunk)).orElse(false))
            chunks.put(pos, chunk);

        mapCache.remove(pos);

        return chunk;
    }

    public Stream<ClaimedChunk> loadedChunks()
    {
        Stream<ClaimedChunk> stream = chunks.values().stream();
        Optional<Stream<ClaimedChunk>> provider = getChunkProvider().map(ChunkProvider::loadedChunks);
        if(provider.isPresent())
            return Stream.concat(stream, provider.get());
        return stream;
    }

    public void reloadChunkSlowly(ChunkPos pos)
    {
        if(!lazyReloads)
        {
            try
            {
                reloadChunk(pos);
            }
            catch(DataSourceException e)
            {
                e.printStackTrace();
            }
            return;
        }

        ClaimedChunk claim = Inconsistency.claim(pos);
        if(!getChunkProvider().map(p-> p.setClaim(claim)).orElse(false))
            chunks.put(pos, claim);
        mapCache.remove(pos);

        reloadQueue.offer(pos);
    }

    public boolean reloadQueuedChunk()
    {
        ChunkPos pos = reloadQueue.poll();
        if(pos == null)
            return false;

        try
        {
            reloadChunk(pos);
        }
        catch(DataSourceException e)
        {
            e.printStackTrace();
        }
        return true;
    }

    public void reloadChunksUnchecked(Predicate<ClaimedChunk> condition)
    {
        loadedChunks().filter(condition).map(ClaimedChunk::getChunk).forEach((DisDBConsumer<ChunkPos>) this::loadChunk);
    }

    public void reloadChunks(Predicate<ClaimedChunk> condition) throws DataSourceException
    {
        try
        {
            loadedChunks().filter(condition).map(ClaimedChunk::getChunk).forEach((DBConsumer<ChunkPos>) this::loadChunk);
        }
        catch(UncheckedDataSourceException e)
        {
            throw e.getCause();
        }
    }

    @Nullable
    public ClaimedChunk reloadChunk(@NotNull ChunkPos pos) throws DataSourceException
    {
        if(!chunks.containsKey(pos) && !getChunkProvider().map(p-> p.getClaim(pos)).isPresent())
            return null;

        return loadChunk(pos);
    }

    @Nullable
    public ClaimedChunk unloadChunk(@NotNull ChunkPos pos)
    {
        ClaimedChunk chunk = chunks.remove(pos);
        return getChunkProvider().map(p-> p.getClaim(pos)).orElse(chunk);
    }

    @Nullable
    public Nature unloadNature(@NotNull WorldDim world)
    {
        Predicate<ChunkPos> condition = c -> c.world.equals(world);
        chunks.keySet().removeIf(condition);
        mapCache.keySet().removeIf(condition);

        Nature nature = natures.remove(world);
        if(nature != null)
            nature.invalidate();
        return nature;
    }

    public Optional<PlayerID> getPlayer(String name) throws DataSourceException
    {
        Optional<PlayerID> result = dataSource.getPlayer(name);
        if(result.isPresent())
            return result;

        return server.getPlayerId(name);
    }
}
