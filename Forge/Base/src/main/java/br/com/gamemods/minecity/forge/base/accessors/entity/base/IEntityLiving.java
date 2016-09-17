package br.com.gamemods.minecity.forge.base.accessors.entity.base;

import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.permission.Permissible;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.EntityPos;
import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemLead;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.command.ForgePlayer;
import br.com.gamemods.minecity.forge.base.protection.reaction.ApproveReaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.NoReaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.Reaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.SingleBlockReaction;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;

import java.util.List;
import java.util.UUID;

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

    default boolean isLeashed()
    {
        return ((EntityLiving) this).getLeashed();
    }

    default IEntity getLeashHolder()
    {
        return (IEntity) ((EntityLiving) this).getLeashedToEntity();
    }

    default boolean canBeLeashedTo(IEntityPlayerMP entity)
    {
        return ((EntityLiving) this).canBeLeashedTo((EntityPlayer) entity);
    }

    @Override
    default Reaction reactPlayerInteraction(ForgePlayer<?, ?, ?> player, IItemStack stack, boolean offHand)
    {
        UUID owner = getEntityOwnerId();
        MineCityForge mod = player.getServer();
        if(isLeashed() && getLeashHolder() == player.cmd.sender)
        {
            EntityPos pos = getEntityPos(mod);
            return new ApproveReaction(pos.getBlock(), PermissionFlag.PICKUP)
                    .addAllowListener((reaction, permissible, flag, p, message) ->
                        mod.consumeItemsOrAddOwnerIf(pos, 1, 1, 1, null, player.identity(), entity->
                                entity.getStack().getIItem() instanceof IItemLead
                        )
                    );
        }
        else if(stack != null && stack.getIItem() instanceof IItemLead && canBeLeashedTo(player.cmd.sender))
        {
            if(player.getUniqueId().equals(owner))
                return NoReaction.INSTANCE;

            return new SingleBlockReaction(getBlockPos(mod), PermissionFlag.MODIFY)
                    .addDenialListener((reaction, permissible, flag, pos, message) ->
                            player.cmd.sender.sendLeashState(this)
                    );
        }

        return reactPlayerInteractLiving(player, stack, offHand);
    }

    default Reaction reactPlayerInteractLiving(ForgePlayer<?,?,?> player, IItemStack stack, boolean offHand)
    {
        return NoReaction.INSTANCE;
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
