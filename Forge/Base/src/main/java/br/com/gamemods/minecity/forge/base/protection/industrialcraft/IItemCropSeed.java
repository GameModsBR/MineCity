package br.com.gamemods.minecity.forge.base.protection.industrialcraft;

import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.ModInterfacesTransformer;

@Referenced(at = ModInterfacesTransformer.class)
public interface IItemCropSeed extends IItemIC2
{
    @Override
    default boolean isHarvest(IItemStack stack)
    {
        return true;
    }
}
