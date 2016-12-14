package br.com.gamemods.minecity.forge.base.protection.immersiveengineering;

import br.com.gamemods.minecity.forge.base.accessors.item.IItem;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.ModInterfacesTransformer;

@Referenced(at = ModInterfacesTransformer.class)
public interface IItemIEBase extends IItem
{
    @Override
    default boolean isHarvest(IItemStack stack)
    {
        return stack.getUnlocalizedName().equals("item.ImmersiveEngineering.material.hempFiber");
    }
}
