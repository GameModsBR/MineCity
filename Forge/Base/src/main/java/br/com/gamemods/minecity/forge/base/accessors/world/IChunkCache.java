package br.com.gamemods.minecity.forge.base.accessors.world;

import br.com.gamemods.minecity.api.world.ChunkPos;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.world.ChunkCacheTransformer;
import br.com.gamemods.minecity.structure.ClaimedChunk;
import br.com.gamemods.minecity.structure.Inconsistency;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Referenced
public interface IChunkCache
{
    @Referenced(at = ChunkCacheTransformer.class)
    int getChunkX();

    @Referenced(at = ChunkCacheTransformer.class)
    int getChunkZ();

    @Referenced(at = ChunkCacheTransformer.class)
    @NotNull
    Chunk[][] getChunks();

    @Referenced(at = ChunkCacheTransformer.class)
    @NotNull
    World getWorld();

    @Nullable
    default Chunk getChunk(int x, int z)
    {
        Chunk[][] chunks = getChunks();
        int ix = x -  getChunkX();
        int iz = z - getChunkZ();

        if(ix < 0 || ix >= chunks.length || iz < 0)
            return null;

        Chunk[] az = chunks[ix];
        if(iz >= az.length)
            return null;

        return az[iz];
    }

    @Nullable
    default IChunk getIChunk(@NotNull ChunkPos pos)
    {
        return (IChunk) getChunk(pos.x, pos.z);
    }

    @NotNull
    default ClaimedChunk getClaim(@NotNull ChunkPos pos)
    {
        IChunk chunk = getIChunk(pos);
        if(chunk == null)
            return Inconsistency.claim(pos);

        ClaimedChunk claim = chunk.getMineCityClaim();
        if(claim == null)
            return Inconsistency.claim(pos);

        return claim;
    }

    @Nullable
    default ClaimedChunk getClaim(int x, int z)
    {
        IChunk chunk = (IChunk) getChunk(x, z);
        if(chunk == null)
            return null;

        return chunk.getMineCityClaim();
    }
}
