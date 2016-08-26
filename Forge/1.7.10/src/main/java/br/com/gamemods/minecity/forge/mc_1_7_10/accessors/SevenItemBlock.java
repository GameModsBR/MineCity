package br.com.gamemods.minecity.forge.mc_1_7_10.accessors;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.accessors.IBlock;
import br.com.gamemods.minecity.forge.base.accessors.IItemBlock;
import br.com.gamemods.minecity.forge.mc_1_7_10.core.transformer.forge.SevenItemBlockTransformer;
import net.minecraft.item.ItemBlock;

@Referenced(at = SevenItemBlockTransformer.class)
public interface SevenItemBlock extends IItemBlock
{
    @Override
    default IBlock getIBlock()
    {
        return (IBlock) ((ItemBlock) this).field_150939_a;
    }
}
