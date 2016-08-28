package br.com.gamemods.minecity.forge.base.accessors.entity;

import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.permission.Permissible;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.protection.reaction.NoReaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.Reaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.SingleBlockReaction;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;

import java.util.List;

public interface IEntityLiving extends IEntityLivingBase
{
    @Override
    default EntityLiving getForgeEntity()
    {
        return (EntityLiving) this;
    }

    default void setTarget(IEntityLivingBase target)
    {
        getForgeEntity().setAttackTarget((EntityLivingBase) target);
    }

    @Override
    default Reaction reactPlayerApplyPotion(MineCityForge mod, Permissible player, IPotionEffect effect,
                                            IEntity potion, List<Permissible> relative)
    {
        if(player.identity().uniqueId.equals(getEntityOwnerId()))
            return NoReaction.INSTANCE;

        if(effect.getName().equals("effect.jump"))
            return new SingleBlockReaction(getBlockPos(mod), PermissionFlag.MODIFY);

        return IEntityLivingBase.super.reactPlayerApplyPotion(mod, player, effect, potion, relative);
    }

    @Override
    default void afterPlayerAttack(MineCityForge mod, Permissible player, IItemStack stack, IEntity entity,
                                   DamageSource source, float amount, List<Permissible> attackers,
                                   Message message)
    {
        if(message == null)
            return;

        if(attackers.contains(this))
            setTarget(null);
    }
}
