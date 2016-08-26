package br.com.gamemods.minecity.forge.mc_1_10_2.accessors.block;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.accessors.block.IBlock;
import br.com.gamemods.minecity.forge.base.accessors.world.IWorldServer;
import br.com.gamemods.minecity.forge.mc_1_10_2.core.transformer.forge.FrostBlockTransformer;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

@Referenced(at = FrostBlockTransformer.class)
public interface FrostBlock extends IBlock
{
    default FrostState getDefaultIState()
    {
        return (FrostState) ((Block) this).getDefaultState();
    }

    @Override
    default boolean isReplaceable(IWorldServer world, int x, int y, int z)
    {
        return ((Block) this).isReplaceable((IBlockAccess) world, new BlockPos(x, y, z));
    }
}
