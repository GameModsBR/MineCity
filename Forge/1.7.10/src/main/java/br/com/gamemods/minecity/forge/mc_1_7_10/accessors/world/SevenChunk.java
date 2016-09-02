package br.com.gamemods.minecity.forge.mc_1_7_10.accessors.world;

import br.com.gamemods.minecity.forge.base.accessors.world.IChunk;
import br.com.gamemods.minecity.forge.base.accessors.world.IWorldServer;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.mc_1_7_10.core.transformer.forge.world.SevenChunkTransformer;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

@Referenced(at = SevenChunkTransformer.class)
public interface SevenChunk extends IChunk
{
    default World getWorld()
    {
        return ((Chunk) this).worldObj;
    }

    @Override
    default IWorldServer getForgeWorldServer()
    {
        return (IWorldServer) ((Chunk) this).worldObj;
    }
}
