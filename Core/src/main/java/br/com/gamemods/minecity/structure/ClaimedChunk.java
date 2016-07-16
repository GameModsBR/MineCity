package br.com.gamemods.minecity.structure;

import br.com.gamemods.minecity.api.ChunkPos;
import org.jetbrains.annotations.NotNull;

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
