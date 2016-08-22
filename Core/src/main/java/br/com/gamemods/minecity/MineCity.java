package br.com.gamemods.minecity;

import br.com.gamemods.minecity.api.DistinctQueue;
import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.Server;
import br.com.gamemods.minecity.api.Slow;
import br.com.gamemods.minecity.api.command.*;
import br.com.gamemods.minecity.api.permission.SimpleFlagHolder;
import br.com.gamemods.minecity.api.world.*;
import br.com.gamemods.minecity.commands.*;
import br.com.gamemods.minecity.datasource.api.DataSourceException;
import br.com.gamemods.minecity.datasource.api.IDataSource;
import br.com.gamemods.minecity.datasource.api.unchecked.DBConsumer;
import br.com.gamemods.minecity.datasource.api.unchecked.DBSupplier;
import br.com.gamemods.minecity.datasource.api.unchecked.DisDBConsumer;
import br.com.gamemods.minecity.datasource.api.unchecked.UncheckedDataSourceException;
import br.com.gamemods.minecity.datasource.sql.SQLSource;
import br.com.gamemods.minecity.structure.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Optional;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class MineCity
{
    public static final Random RANDOM = new Random();
    @NotNull
    public final IDataSource dataSource;
    public final Server server;
    public final CommandTree commands = new CommandTree();
    private final ConcurrentHashMap<WorldDim, Nature> natures = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<ChunkPos, ClaimedChunk> chunks = new ConcurrentHashMap<>();
    public final ConcurrentHashMap<ChunkPos, CityCommand.MapCache> mapCache = new ConcurrentHashMap<>();
    public final Queue<EntityUpdate> entityUpdates = new ConcurrentLinkedQueue<>();
    public ExecutorService mapService = Executors.newSingleThreadExecutor(r-> new Thread(r, "MineCityMapService"));
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public Optional<WorldProvider> worldProvider = Optional.empty();
    public MessageTransformer messageTransformer;
    public SimpleFlagHolder defaultNatureFlags = new SimpleFlagHolder();
    public SimpleFlagHolder defaultCityFlags = new SimpleFlagHolder();
    public SimpleFlagHolder defaultPlotFlags = new SimpleFlagHolder();
    public SimpleFlagHolder defaultReserveFlags = new SimpleFlagHolder();
    private Queue<ChunkPos> reloadQueue = new DistinctQueue<>();
    public boolean lazyReloads = true;

    @SuppressWarnings("LanguageMismatch")
    public MineCity(@NotNull Server server, @NotNull MineCityConfig config, @Nullable IDataSource dataSource,
                    @NotNull MessageTransformer messageTransformer)
    {
        defaultCityFlags = config.defaultCityFlags;
        defaultNatureFlags = config.defaultNatureFlags;
        defaultPlotFlags = config.defaultPlotFlags;
        defaultReserveFlags = config.defaultReserveFlags;

        this.server = server;
        this.messageTransformer = messageTransformer;
        this.dataSource = dataSource == null? new SQLSource(this, config) : dataSource;
        if(config.dbPass != null)
            Arrays.fill(config.dbPass, (byte) 0);

        commands.dataSource = this.dataSource;
        commands.onlinePlayers = server::getOnlinePlayerNames;
        commands.cityNames = this.dataSource.cityNameSupplier();
        commands.scheduler = server::runAsynchronously;
        commands.messageTransformer = ()-> messageTransformer;
        commands.optionTransformer = (id, args) ->
            Arrays.stream(args).map(arg ->
            {
                String[] options = arg.options();
                if(options.length == 0)
                    return arg;

                for(int i = 0; i < options.length; i++)
                {
                    String option = options[i];
                    options[i] = messageTransformer.toSimpleText(new Message(
                            "cmd."+id+".arg."+arg.name().toLowerCase().replaceAll("[^a-z0-9]+", "-")+".option."+
                                    option.toLowerCase().replaceAll("[^a-z-0-9]+","-"),
                            option
                    )).replaceAll("\\s","");
                }

                return new TranslatedOptions(arg, options);
            }).toArray(Arg[]::new)
        ;
        commands.registerCommands(new CityCommand(this));
        new PermissionCommands(this).register(commands);
        commands.registerCommands(new GroupCommand(this));
        commands.registerCommands(GeneralCommands.class);
        commands.registerCommands(PlotCommand.class);
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
    public Optional<City> getCity(@NotNull ChunkPos pos)
    {
        return getChunk(pos).flatMap(ClaimedChunk::getCity);
    }

    @NotNull
    public Optional<Plot> getPlot(@NotNull BlockPos pos)
    {
        return getChunk(pos.getChunk()).flatMap(c-> c.getPlotAt(pos));
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
    public ClaimedChunk provideChunk(@NotNull ChunkPos pos, @Nullable ClaimedChunk cache)
    {
        if(cache != null && !cache.isInvalid() && pos.equals(cache.chunk))
            return cache;

        return provideChunk(pos);
    }

    @NotNull
    public ClaimedChunk provideChunk(@NotNull ChunkPos pos)
    {
        return getChunk(pos).orElseGet(()-> Inconsistency.claim(pos));
    }

    @NotNull
    public ChunkPos provideChunk(@NotNull WorldDim world, int x, int z)
    {
        return getChunkProvider().map(p-> p.getChunk(world, x, z)).orElseGet(()-> new ChunkPos(world, x, z));
    }

    /**
     * Gets a chunk claim data from the memory when it's available or fetches from the database when it's not
     * @return Empty optional if the chunk is unloaded and not claimed. Non-empty optional may still contains an
     * unclaimed chunk that is already loaded.
     */
    @Slow
    public Optional<ClaimedChunk> getOrFetchChunkUnchecked(@NotNull ChunkPos pos)
    {
        return Optional.ofNullable(getChunk(pos).orElseGet((DBSupplier<ClaimedChunk>) () -> dataSource.getCityChunk(pos)));
    }

    public Optional<ClaimedChunk> getOrFetchChunk(@NotNull ChunkPos pos) throws DataSourceException
    {
        try
        {
            return getOrFetchChunkUnchecked(pos);
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
    public Nature loadNature(@NotNull WorldDim world) throws DataSourceException
    {
        Nature nature = natures.get(world);
        if(nature != null)
            unloadNature(world);

        nature = dataSource.getNature(world);
        world.nature = nature;
        natures.put(world, nature);
        return nature;
    }

    @NotNull
    public Nature nature(@NotNull WorldDim world)
    {
        Nature nature = getNature(world);
        if(nature != null && nature.isValid()) return nature;
        try
        {
            return loadNature(world);
        }
        catch(DataSourceException e)
        {
            return Inconsistency.nature(world);
        }
    }

    public Optional<ChunkProvider> getChunkProvider()
    {
        return worldProvider.flatMap(WorldProvider::getChunkProvider);
    }

    @Slow
    @NotNull
    public ClaimedChunk loadChunk(@NotNull ChunkPos pos) throws DataSourceException
    {
        ClaimedChunk chunk = Optional.ofNullable(dataSource.getCityChunk(pos))
                .orElseGet(()-> new ClaimedChunk(nature(pos.world), pos));

        if(!getChunkProvider().map(p-> p.setClaim(chunk)).orElse(false))
        {
            ClaimedChunk removed = chunks.put(pos, chunk);
            if(removed != null)
                removed.invalidate();
        }

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

    @Slow
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
        {
            ClaimedChunk removed = chunks.put(pos, claim);
            if(removed != null)
                removed.invalidate();
        }
        mapCache.remove(pos);

        reloadQueue.offer(pos);
    }

    @Slow
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

    @Slow
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

    @Slow
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
        chunk = getChunkProvider().map(p-> p.getClaim(pos)).orElse(chunk);

        if(chunk != null)
            chunk.invalidate();
        return chunk;
    }

    @Nullable
    public Nature unloadNature(@NotNull WorldDim world)
    {
        chunks.entrySet().removeIf(e-> {
            if(e.getKey().world.equals(world))
            {
                e.getValue().invalidate();
                return true;
            }
            return false;
        });
        mapCache.keySet().removeIf(c -> c.world.equals(world));

        Nature nature = natures.remove(world);
        if(nature != null)
            nature.invalidate();
        return nature;
    }

    @Slow
    public Optional<PlayerID> getPlayer(String name) throws DataSourceException
    {
        Optional<PlayerID> result = dataSource.getPlayer(name);
        if(result.isPresent())
            return result;

        return server.getPlayerId(name);
    }

    @Slow
    public Optional<PlayerID> findPlayer(String playerName) throws UncheckedDataSourceException
    {
        Optional<PlayerID> playerId = server.getPlayerId(playerName);
        if(playerId.isPresent())
            return playerId;

        try
        {
            return dataSource.getPlayer(playerName);
        }
        catch(DataSourceException e)
        {
            throw new UncheckedDataSourceException(e);
        }
    }
}
