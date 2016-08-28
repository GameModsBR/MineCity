package br.com.gamemods.minecity.forge.mc_1_7_10.accessors.item;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.mc_1_7_10.core.transformer.forge.SevenInterfaceTransformer;

@Referenced(at = SevenInterfaceTransformer.class)
public interface SevenItemStack extends IItemStack
{
    @Override
    default int getMeta()
    {
        return getStack().getItemDamage();
    }
}
