package br.com.gamemods.minecity.forge.base.accessors.item;

import br.com.gamemods.minecity.forge.base.accessors.block.IBlock;

public interface ItemBlockBase extends IItem
{
    IBlock getIBlock();

    @Override
    default boolean isHarvest(IItemStack stack)
    {
        return getIBlock().isHarvest();
    }
}
