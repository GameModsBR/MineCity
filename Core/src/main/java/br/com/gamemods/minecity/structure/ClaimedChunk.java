package br.com.gamemods.minecity.structure;

import br.com.gamemods.minecity.api.permission.FlagHolder;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.ChunkPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class ClaimedChunk
{
    @NotNull
    public final ChunkOwner owner;
    @NotNull
    public final ChunkPos chunk;
    public final boolean reserve;
    @Nullable
    private Set<Plot> plots;
    private boolean invalid;

    public ClaimedChunk(@NotNull ChunkOwner owner, @NotNull ChunkPos chunk)
    {
        this.owner = owner;
        this.chunk = chunk;
        this.reserve = owner instanceof Reserve;
    }

    public ClaimedChunk(@NotNull Island owner, @NotNull ChunkPos chunk, boolean reserve)
    {
        this.owner = reserve? owner.reserve : owner;
        this.chunk = chunk;
        this.reserve = reserve;
    }

    public Optional<Plot> getPlotAt(BlockPos pos)
    {
        if(!pos.world.equals(chunk.world))
            return Optional.empty();

        return getPlotAt(pos.x, pos.y, pos.z);
    }

    public Optional<Plot> getPlotAt(int x, int y, int z)
    {
        for(Plot plot : getPlots())
            if(plot.getShape().contains(x, y, z))
                return Optional.of(plot);

        return Optional.empty();
    }

    public Collection<Plot> getPlots()
    {
        if(plots != null)
            return plots;

        if(reserve)
            return plots = Collections.emptySet();

        return plots = getIsland().map(i -> i.getPlotsAt(chunk)).orElse(Stream.empty()).collect(Collectors.toSet());
    }

    public Optional<Island> getIslandAcceptingReserve()
    {
        if(!reserve)
            return getIsland();

        return Optional.of(((Reserve)owner).island);
    }

    @NotNull
    public Optional<Island> getIsland()
    {
        if(owner instanceof Island) return Optional.of((Island) owner);
        if(owner instanceof Inconsistency) return Optional.of(Inconsistency.getInconsistentIsland());
        return Optional.empty();
    }

    public Optional<Reserve> getReserve()
    {
        if(owner instanceof Reserve) return Optional.of((Reserve)owner);
        return Optional.empty();
    }

    public Optional<Nature> nature()
    {
        Nature nature = chunk.world.nature;
        if(nature != null)
            return Optional.of(nature);

        if(owner instanceof Nature)
            return Optional.of((Nature) owner);

        return Optional.empty();
    }

    /**
     * @throws NoSuchElementException If the chunk is reserved but the nature object is not available
     */
    @NotNull
    public FlagHolder getFlagHolder()
    {
        if(reserve)
            return (Reserve) owner;

        return getIsland().<FlagHolder>map(Island::getCity).orElse(chunk.world.nature);
    }

    /**
     * @throws NoSuchElementException If the chunk is reserved but the nature object is not available
     */
    @NotNull
    public FlagHolder getFlagHolder(int blockX, int blockY, int blockZ)
    {
        if(reserve)
            return (Reserve) owner;

        Optional<Plot> plot = getPlotAt(blockX, blockY, blockZ);
        if(plot.isPresent())
            return plot.get();

        return getIsland().<FlagHolder>map(Island::getCity).orElse(chunk.world.nature);
    }

    public FlagHolder getFlagHolder(BlockPos pos)
    {
        return getFlagHolder(pos.x, pos.y, pos.z);
    }

    @NotNull
    public Optional<City> getCity()
    {
        return getIsland().map(Island::getCity);
    }

    public Optional<City> getCityAcceptingReserve()
    {
        if(!reserve)
            return getCity();

        return Optional.of(((Reserve)owner).island.getCity());
    }

    @NotNull
    public ChunkOwner getOwner()
    {
        return owner;
    }

    @NotNull
    public ChunkPos getChunk()
    {
        return chunk;
    }

    public boolean isInvalid()
    {
        return invalid;
    }

    public void invalidate()
    {
        invalid = true;
    }

    @Override
    public String toString()
    {
        return "CityChunk{" +
                "owner=" + owner +
                ", chunk=" + chunk +
                '}';
    }

    @Override
    public boolean equals(Object o)
    {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;

        ClaimedChunk that = (ClaimedChunk) o;
        return owner.equals(that.owner) && chunk.equals(that.chunk);
    }

    @Override
    public int hashCode()
    {
        int result = owner.hashCode();
        result = 31*result + chunk.hashCode();
        return result;
    }
}
