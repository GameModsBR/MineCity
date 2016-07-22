package br.com.gamemods.minecity.forge.accessors;

import br.com.gamemods.minecity.api.world.ChunkPos;
import br.com.gamemods.minecity.structure.ClaimedChunk;
import net.minecraft.world.chunk.Chunk;

public interface IChunk
{
    ClaimedChunk getMineCityClaim();
    void setMineCityClaim(ClaimedChunk claim);

    default ChunkPos getMineCityChunk()
    {
        ClaimedChunk claim = getMineCityClaim();
        if(claim == null)
            return null;

        return claim.chunk;
    }

    default Chunk getForgeChunk()
    {
        return (Chunk) this;
    }
}
