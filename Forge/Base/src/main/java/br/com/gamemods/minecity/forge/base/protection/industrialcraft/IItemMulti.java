package br.com.gamemods.minecity.forge.base.protection.industrialcraft;

import br.com.gamemods.minecity.forge.base.accessors.item.IItem;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.ModInterfacesTransformer;

@Referenced(at = ModInterfacesTransformer.class)
public interface IItemMulti extends IItem
{
    @Override
    default boolean isHarvest(IItemStack stack)
    {
        return stack.getUnlocalizedName().equals("ic2.misc_resource.resin");
    }
}
