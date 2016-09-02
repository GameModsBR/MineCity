package br.com.gamemods.minecity.forge.base.accessors.entity.base;

import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.ForgeInterfaceTransformer;
import net.minecraft.entity.EntityAgeable;

@Referenced(at = ForgeInterfaceTransformer.class)
public interface IEntityAgeable extends IEntityCreature
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
