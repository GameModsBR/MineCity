package br.com.gamemods.minecity.sponge.core.mixin;

import br.com.gamemods.minecity.sponge.core.mixed.MixedEntityLivingBase;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EntityLivingBase.class)
public abstract class MixinEntityLivingBase implements MixedEntityLivingBase
{
    private EntityLivingBase self()
    {
        return ((EntityLivingBase) (Object) this);
    }

    @Override
    public boolean isFlyingOnElytra()
    {
        return self().isElytraFlying();
    }

    @Override
    public ItemStack getActiveStack()
    {
        return (ItemStack) (Object) self().getActiveItemStack();
    }
}
