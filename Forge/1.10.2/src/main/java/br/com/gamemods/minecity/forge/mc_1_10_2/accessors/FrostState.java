package br.com.gamemods.minecity.forge.mc_1_10_2.accessors;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.accessors.IState;
import br.com.gamemods.minecity.forge.mc_1_10_2.core.transformer.forge.FrostIBlockStateTransformer;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraftforge.fml.common.registry.GameData;

@Referenced(at = FrostIBlockStateTransformer.class)
public interface FrostState extends IState
{
    default IBlockState getForgeState()
    {
        return (IBlockState) this;
    }

    default Block getForgeBlock()
    {
        return ((IBlockState) this).getBlock();
    }

    default FrostBlock getIBlock()
    {
        return (FrostBlock) ((IBlockState) this).getBlock();
    }

    default boolean isOpaqueCube()
    {
        return getForgeState().isOpaqueCube();
    }

    @Override
    default int getStateId()
    {
        return GameData.getBlockStateIDMap().get((IBlockState) this);
    }
}
