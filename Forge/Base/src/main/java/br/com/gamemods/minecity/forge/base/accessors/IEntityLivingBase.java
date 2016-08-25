package br.com.gamemods.minecity.forge.base.accessors;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.EntityLivingBaseTransformer;
import net.minecraft.entity.EntityLivingBase;

@Referenced(at = EntityLivingBaseTransformer.class)
public interface IEntityLivingBase extends IEntity
{
    default EntityLivingBase getEntityLivingBase()
    {
        return (EntityLivingBase) this;
    }

    default boolean isElytraFlying()
    {
        return getEntityLivingBase().isElytraFlying();
    }
}
