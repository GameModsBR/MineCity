package br.com.gamemods.minecity.forge.mc_1_10_2.accessors;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.accessors.IBlock;
import br.com.gamemods.minecity.forge.mc_1_10_2.core.transformer.forge.FrostBlockTransformer;
import net.minecraft.block.Block;

@Referenced(at = FrostBlockTransformer.class)
public interface FrostBlock extends IBlock
{
    default FrostState getDefaultIState()
    {
        return (FrostState) ((Block) this).getDefaultState();
    }
}
