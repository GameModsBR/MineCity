package br.com.gamemods.minecity.sponge.core.mixin;

import br.com.gamemods.minecity.sponge.core.mixed.MixedEntityLivingBase;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EntityLivingBase.class)
public abstract class MixinEntityLivingBase implements MixedEntityLivingBase
{
    @Override
    public boolean isFlyingOnElytra()
    {
        return ((EntityLivingBase) (Object) this).isElytraFlying();
    }

    @Override
    public ItemStack getActiveStack()
    {
        return (ItemStack) (Object) ((EntityLivingBase) (Object) this).getActiveItemStack();
    }
}
