package br.com.gamemods.minecity.structure;

import br.com.gamemods.minecity.api.world.ChunkPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ClaimedChunk
{
    @NotNull
    public final ChunkOwner owner;
    @NotNull
    public final ChunkPos chunk;

    public ClaimedChunk(@NotNull ChunkOwner owner, @NotNull ChunkPos chunk)
    {
        this.owner = owner;
        this.chunk = chunk;
    }

    @Nullable
    public Island getIsland()
    {
        if(owner instanceof Island) return (Island) owner;
        if(owner instanceof Inconsistency) return Inconsistency.getInconsistentIsland();
        return null;
    }

    @Nullable
    public City getCity()
    {
        Island island = getIsland();
        if(island == null) return null;
        return island.getCity();
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
