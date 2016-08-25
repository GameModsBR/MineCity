package br.com.gamemods.minecity.forge.mc_1_7_10.accessors;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.accessors.IBlock;
import br.com.gamemods.minecity.forge.base.accessors.IState;
import br.com.gamemods.minecity.forge.mc_1_7_10.core.transformer.forge.SevenBlockTransformer;
import net.minecraft.block.Block;

@Referenced(at = SevenBlockTransformer.class)
public interface SevenBlock extends IBlock, IState
{
    @Override
    default IBlock getIBlock()
    {
        return this;
    }

    @Override
    default Block getForgeBlock()
    {
        return (Block) this;
    }
}
