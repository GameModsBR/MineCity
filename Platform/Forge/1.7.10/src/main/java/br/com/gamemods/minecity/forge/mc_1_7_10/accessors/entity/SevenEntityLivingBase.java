package br.com.gamemods.minecity.forge.mc_1_7_10.accessors.entity;

import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityLivingBase;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.mc_1_7_10.core.transformer.forge.SevenInterfaceTransformer;
import net.minecraft.entity.EntityLivingBase;

@Referenced(at = SevenInterfaceTransformer.class)
public interface SevenEntityLivingBase extends IEntityLivingBase, SevenEntity
{
    @Override
    default boolean isElytraFlying()
    {
        return false;
    }

    @Override
    default IItemStack getStackInHand(boolean offHand)
    {
        if(offHand)
            return null;
        else
            return (IItemStack) (Object) ((EntityLivingBase) this).getHeldItem();
    }

    @Override
    default void setPosAndUpdate(double x, double y, double z)
    {
        ((EntityLivingBase) this).setPositionAndUpdate(x, y, z);
    }

    @Override
    default IItemStack getActiveItemStack()
    {
        return null;
    }

    @Override
    default int getActiveItemUseCount()
    {
        return 0;
    }

    @Override
    default void stopUsingItem()
    {
        // Unsupported
    }
}
