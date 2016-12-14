package br.com.gamemods.minecity.forge.base.protection.immersiveengineering;

import br.com.gamemods.minecity.forge.base.accessors.item.IItemSeeds;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.ModInterfacesTransformer;

@Referenced(at = ModInterfacesTransformer.class)
public interface IItemIESeed extends IItemIEBase, IItemSeeds
{
    @Override
    default boolean isHarvest(IItemStack stack)
    {
        return true;
    }
}
