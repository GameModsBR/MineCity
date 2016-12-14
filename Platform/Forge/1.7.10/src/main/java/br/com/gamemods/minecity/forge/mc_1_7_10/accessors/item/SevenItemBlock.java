package br.com.gamemods.minecity.forge.mc_1_7_10.accessors.item;

import br.com.gamemods.minecity.forge.base.accessors.block.IBlock;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemBlock;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.mc_1_7_10.core.transformer.forge.SevenInterfaceTransformer;
import net.minecraft.item.ItemBlock;

@Referenced(at = SevenInterfaceTransformer.class)
public interface SevenItemBlock extends IItemBlock
{
    @Override
    default IBlock getIBlock()
    {
        return (IBlock) ((ItemBlock) this).field_150939_a;
    }
}
