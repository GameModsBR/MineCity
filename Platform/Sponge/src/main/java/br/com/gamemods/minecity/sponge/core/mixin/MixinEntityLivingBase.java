package br.com.gamemods.minecity.sponge.core.mixin;

import br.com.gamemods.minecity.sponge.core.mixed.MixedEntityLivingBase;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(EntityLivingBase.class)
public abstract class MixinEntityLivingBase implements MixedEntityLivingBase
{
    @Shadow
    public abstract boolean isElytraFlying();

    @Shadow
    public abstract net.minecraft.item.ItemStack getActiveItemStack();

    @Override
    public boolean isFlyingOnElytra()
    {
        return isElytraFlying();
    }

    @Override
    public ItemStack getActiveStack()
    {
        return (ItemStack) (Object) getActiveItemStack();
    }
}
