package br.com.gamemods.minecity.forge.mc_1_7_10.accessors.entity;

import br.com.gamemods.minecity.forge.base.accessors.entity.item.IEntityItem;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.mc_1_7_10.core.transformer.forge.SevenInterfaceTransformer;
import net.minecraft.entity.item.EntityItem;

@Referenced(at = SevenInterfaceTransformer.class)
public interface SevenEntityItem extends IEntityItem, SevenEntity
{
    @Override
    default String getItemOwner()
    {
        return getForgeEntity().func_145798_i();
    }

    @Override
    default void setItemOwner(String name)
    {
        getForgeEntity().func_145797_a(name);
    }

    @Override
    default String getItemThrower()
    {
        return ((EntityItem) this).func_145800_j();
    }
}
