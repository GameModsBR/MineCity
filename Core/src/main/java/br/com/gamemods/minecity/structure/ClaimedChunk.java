package br.com.gamemods.minecity.structure;

import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.ChunkPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
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

    public ClaimedChunk(@NotNull ChunkOwner owner, @NotNull ChunkPos chunk)
    {
        this.owner = owner;
        this.chunk = chunk;
        this.reserve = false;
    }

    public ClaimedChunk(@NotNull ChunkOwner owner, @NotNull ChunkPos chunk, boolean reserve)
    {
        this.owner = owner;
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
        return plots = getIsland().map(i -> i.getPlotsAt(chunk)).orElse(Stream.empty()).collect(Collectors.toSet());
    }

    @NotNull
    public Optional<Island> getIsland()
    {
        if(owner instanceof Island) return Optional.of((Island) owner);
        if(owner instanceof Inconsistency) return Optional.of(Inconsistency.getInconsistentIsland());
        return Optional.empty();
    }

    @NotNull
    public Optional<City> getCity()
    {
        return getIsland().map(Island::getCity);
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
