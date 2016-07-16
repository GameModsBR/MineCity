package br.com.gamemods.minecity;

import br.com.gamemods.minecity.api.BlockPos;
import br.com.gamemods.minecity.api.ChunkPos;
import br.com.gamemods.minecity.api.WorldDim;
import br.com.gamemods.minecity.datasource.DataSourceException;
import br.com.gamemods.minecity.datasource.IDataSource;
import br.com.gamemods.minecity.datasource.sql.SQLSource;
import br.com.gamemods.minecity.structure.City;
import br.com.gamemods.minecity.structure.ClaimedChunk;
import br.com.gamemods.minecity.structure.Nature;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class MineCity
{
    @NotNull
    public final IDataSource dataSource;
    private final ConcurrentHashMap<WorldDim, Nature> natures = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<ChunkPos, ClaimedChunk> chunks = new ConcurrentHashMap<>();

    public MineCity(@NotNull MineCityConfig config, @Nullable IDataSource dataSource)
    {
        this.dataSource = dataSource == null? new SQLSource(this, config) : dataSource;
        if(config.dbPass != null)
            Arrays.fill(config.dbPass, (byte) 0);
    }

    public MineCity(MineCityConfig config)
    {
        this(config, null);
    }

    @Nullable
    public ClaimedChunk getChunk(@NotNull BlockPos pos)
    {
        return getChunk(pos.getChunk());
    }

    @Nullable
    public ClaimedChunk getChunk(@NotNull ChunkPos pos)
    {
        return chunks.get(pos);
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
