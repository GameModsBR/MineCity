package br.com.gamemods.minecity.forge.mc_1_7_10.accessors;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.accessors.IWorldServer;
import br.com.gamemods.minecity.forge.mc_1_7_10.core.transformer.forge.Forge7ChunkTransformer;
import net.minecraft.world.World;

@Referenced(at = Forge7ChunkTransformer.class)
public interface IChunk7 extends br.com.gamemods.minecity.forge.base.accessors.IChunk
{
    default World getWorld()
    {
        return getForgeChunk().worldObj;
    }

    @Override
    default IWorldServer getForgeWorldServer()
    {
        return (IWorldServer) getForgeChunk().worldObj;
    }
}
