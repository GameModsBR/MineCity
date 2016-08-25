package br.com.gamemods.minecity.forge.base.accessors;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.BlockTransformer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraftforge.fluids.BlockFluidBase;

@Referenced(at = BlockTransformer.class)
public interface IBlock
{
    default Block getForgeBlock()
    {
        return (Block) this;
    }

    IState getDefaultIState();

    default boolean isLiquid()
    {
        return this instanceof BlockLiquid || this instanceof BlockFluidBase;
    }
}
