package br.com.gamemods.minecity.forge.base.accessors.entity;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.ForgeInterfaceTransformer;
import net.minecraft.entity.EntityLivingBase;

@Referenced(at = ForgeInterfaceTransformer.class)
public interface IEntityLivingBase extends IEntity
{
    @Override
    default EntityLivingBase getForgeEntity()
    {
        return (EntityLivingBase) this;
    }

    default boolean isElytraFlying()
    {
        return getForgeEntity().isElytraFlying();
    }

    default IItemStack getStackInHand(boolean offHand)
    {
        EntityLivingBase entity = (EntityLivingBase) this;
        if(offHand)
            return (IItemStack) (Object) entity.getHeldItemOffhand();
        else
            return (IItemStack) (Object) entity.getHeldItemMainhand();
    }
}
