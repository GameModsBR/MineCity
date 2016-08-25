package br.com.gamemods.minecity.forge.mc_1_7_10.accessors;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.accessors.IWorldServer;
import br.com.gamemods.minecity.forge.mc_1_7_10.core.transformer.forge.SevenChunkTransformer;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

@Referenced(at = SevenChunkTransformer.class)
public interface SevenChunk extends br.com.gamemods.minecity.forge.base.accessors.IChunk
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
