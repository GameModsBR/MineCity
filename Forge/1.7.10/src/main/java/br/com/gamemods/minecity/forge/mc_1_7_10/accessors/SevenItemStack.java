package br.com.gamemods.minecity.forge.mc_1_7_10.accessors;

import br.com.gamemods.minecity.forge.base.accessors.IItemStack;

public interface SevenItemStack extends IItemStack
{
    @Override
    default int getMeta()
    {
        return getStack().getItemDamage();
    }
}
