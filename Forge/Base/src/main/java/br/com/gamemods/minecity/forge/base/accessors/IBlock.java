package br.com.gamemods.minecity.forge.base.accessors;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.BlockTransformer;
import net.minecraft.block.Block;

@Referenced(at = BlockTransformer.class)
public interface IBlock
{
    default Block getForgeBlock()
    {
        return (Block) this;
    }
}
