package br.com.gamemods.minecity.forge.base.accessors.entity;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.ForgeInterfaceTransformer;
import net.minecraft.entity.EntityAgeable;

@Referenced(at = ForgeInterfaceTransformer.class)
public interface IEntityAgeable extends IEntityLiving
{
    @Override
    default EntityAgeable getForgeEntity()
    {
        return (EntityAgeable) this;
    }

    default boolean isChild()
    {
        return getForgeEntity().isChild();
    }
}
