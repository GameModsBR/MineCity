package br.com.gamemods.minecity.forge.base.accessors.entity;

import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.permission.Permissible;
import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.ForgeInterfaceTransformer;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.entity.EntityArrowTransformer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;

import java.util.List;

@Referenced(at = ForgeInterfaceTransformer.class)
public interface IEntityArrow extends EntityProjectile
{
    @Override
    default EntityArrow getForgeEntity()
    {
        return (EntityArrow) this;
    }

    @Referenced(at = EntityArrowTransformer.class)
    default IItemStack getIArrowStack()
    {
        return (IItemStack) (Object) new ItemStack(Items.ARROW);
    }

    @Override
    default void detectShooter(MineCityForge mod)
    {
        IEntity shooter = (IEntity) getForgeEntity().shootingEntity;
        if(shooter == null)
            setShooter(new ProjectileShooter(getEntityPos(mod)));
        else
            setShooter(new ProjectileShooter(getEntityPos(mod), shooter));
    }

    @Override
    default void afterPlayerAttack(MineCityForge mod, Permissible player, IItemStack stack, IEntity entity,
                                   DamageSource source, float amount, List<Permissible> attackers,
                                   Message message)
    {
        if(message != null)
            getForgeEntity().setDead();
    }
}
