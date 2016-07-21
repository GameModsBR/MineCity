package br.com.gamemods.minecity;

import br.com.gamemods.minecity.api.command.CommandTree;
import br.com.gamemods.minecity.api.command.MessageTransformer;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.ChunkPos;
import br.com.gamemods.minecity.api.world.WorldDim;
import br.com.gamemods.minecity.commands.CityCommand;
import br.com.gamemods.minecity.datasource.api.DataSourceException;
import br.com.gamemods.minecity.datasource.api.IDataSource;
import br.com.gamemods.minecity.datasource.api.unchecked.UncheckedDataSourceException;
import br.com.gamemods.minecity.datasource.sql.SQLSource;
import br.com.gamemods.minecity.structure.ClaimedChunk;
import br.com.gamemods.minecity.structure.Inconsistency;
import br.com.gamemods.minecity.structure.Nature;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class MineCity
{
    @NotNull
    public final IDataSource dataSource;
    public final CommandTree commands = new CommandTree();
    private final ConcurrentHashMap<WorldDim, Nature> natures = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<ChunkPos, ClaimedChunk> chunks = new ConcurrentHashMap<>();
    public MessageTransformer messageTransformer;

    public MineCity(@NotNull MineCityConfig config, @Nullable IDataSource dataSource,
                    @NotNull MessageTransformer messageTransformer)
    {
        this.messageTransformer = messageTransformer;
        this.dataSource = dataSource == null? new SQLSource(this, config) : dataSource;
        if(config.dbPass != null)
            Arrays.fill(config.dbPass, (byte) 0);

        commands.registerCommands(new CityCommand(this));
        Inconsistency.setMineCity(this);
    }

    public MineCity(MineCityConfig config)
    {
        this(config, null, new MessageTransformer());
    }

    public MineCity(MineCityConfig config, MessageTransformer messageTransformer)
    {
        this(config, null, messageTransformer);
    }

    public Map<ChunkPos, ClaimedChunk> loadedChunks()
    {
        return Collections.unmodifiableMap(chunks);
    }

    @NotNull
    public Optional<ClaimedChunk> getChunk(@NotNull BlockPos pos)
    {
        return getChunk(pos.getChunk());
    }

    @NotNull
    public Optional<ClaimedChunk> getChunk(@NotNull ChunkPos pos)
    {
        return Optional.ofNullable(chunks.get(pos));
    }

    public Optional<ClaimedChunk> getOrFetchChunk(@NotNull ChunkPos pos) throws DataSourceException
    {
        ClaimedChunk claimedChunk = chunks.get(pos);
        if(claimedChunk != null) return Optional.of(claimedChunk);

        return Optional.ofNullable(dataSource.getCityChunk(pos));
    }

    @Nullable
    public Nature getNature(@NotNull WorldDim world)
    {
        return natures.get(world);
    }

    @NotNull
    public Nature loadNature(@NotNull WorldDim world)
    {
        Nature nature = new Nature(world);
        natures.put(world, nature);
        return nature;
    }

    @NotNull
    public Nature nature(@NotNull WorldDim world)
    {
        Nature nature = natures.get(world);
        if(nature != null) return nature;
        return loadNature(world);
    }

    @NotNull
    public ClaimedChunk loadChunk(@NotNull ChunkPos pos) throws DataSourceException
    {
        ClaimedChunk chunk = dataSource.getCityChunk(pos);
        if(chunk != null) chunks.put(pos, chunk);
        else chunks.put(pos, chunk = new ClaimedChunk(nature(pos.world), pos));

        return chunk;
    }

    @Nullable
    public ClaimedChunk reloadChunk(@NotNull ChunkPos pos) throws DataSourceException
    {
        if(!chunks.containsKey(pos))
            return null;

        return loadChunk(pos);
    }

    @Nullable
    public ClaimedChunk unloadChunk(@NotNull ChunkPos pos)
    {
        return chunks.remove(pos);
    }

    @Nullable
    public Nature unloadNature(@NotNull WorldDim world)
    {
        chunks.keySet().removeIf(c-> c.world.equals(world));
        return natures.remove(world);
    }
}
