package br.com.gamemods.minecity.forge.mc_1_7_10.accessors.item;

import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;

public interface SevenItemStack extends IItemStack
{
    @Override
    default int getMeta()
    {
        return getStack().getItemDamage();
    }
}
