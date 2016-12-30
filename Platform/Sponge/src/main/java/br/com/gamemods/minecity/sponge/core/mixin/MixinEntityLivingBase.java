package br.com.gamemods.minecity.sponge.core.mixin;

import br.com.gamemods.minecity.sponge.core.mixed.MixedEntityLivingBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EntityLivingBase.class)
public abstract class MixinEntityLivingBase extends EntityLivingBase implements MixedEntityLivingBase
{
    public MixinEntityLivingBase(World worldIn)
    {
        super(worldIn);
    }

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
