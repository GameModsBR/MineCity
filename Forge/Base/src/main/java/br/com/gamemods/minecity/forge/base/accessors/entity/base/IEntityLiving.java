package br.com.gamemods.minecity.forge.base.accessors.entity.base;

import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.permission.*;
import br.com.gamemods.minecity.api.unchecked.BiIntFunction;
import br.com.gamemods.minecity.api.world.EntityPos;
import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemLead;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.command.ForgePlayer;
import br.com.gamemods.minecity.forge.base.protection.reaction.ApproveReaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.NoReaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.Reaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.SingleBlockReaction;
import br.com.gamemods.minecity.structure.ClaimedChunk;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.DamageSource;
import net.minecraft.world.IBlockAccess;

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

    @SuppressWarnings("SimplifiableIfStatement")
    default boolean canGoFromTo(PathFinder pathFinder, PathPoint point, IBlockAccess access, PermissionFlag flag,
                                ClaimedChunk from, ClaimedChunk to, FlagHolder fromHolder, FlagHolder toHolder)
    {
        Identity<?> fromOwner = fromHolder.owner();
        Identity<?> toOwner = toHolder.owner();
        if(flag == null || fromHolder == toHolder || fromOwner.equals(toOwner))
            return true;

        Identity<?> owner = getPlayerOwner();
        if(owner == null)
            owner = fromOwner;

        if(owner.equals(toOwner))
            return true;

        if(owner.getType() == Identity.Type.NATURE)
        {
            switch(flag)
            {
                case PVM:
                    if(toHolder instanceof SimpleFlagHolder)
                        return !((SimpleFlagHolder) toHolder).can(flag).isPresent();
                    return true;

                default:
                    return false;
            }
        }

        return !toHolder.can(owner, PermissionFlag.ENTER).isPresent() && !toHolder.can(owner, flag).isPresent()
                && !fromHolder.can(toOwner, PermissionFlag.ENTER).isPresent() && !fromHolder.can(toOwner, flag).isPresent();
    }

    default int canGoFromToWidth()
    {
        return (int) Math.floor(((EntityLiving) this).width + 1);
    }

    default int canGoFromToHeight()
    {
        return 1;
    }

    default PermissionFlag getPathFinderFlag(BiIntFunction<ClaimedChunk> getClaim, ClaimedChunk from, FlagHolder fromHolder,
                                             PathFinder pathFinder, PathPoint point, IBlockAccess access)
    {
        return getPlayerAttackType();
    }

    default boolean canGoFromTo(BiIntFunction<ClaimedChunk> getClaim, ClaimedChunk from, FlagHolder fromHolder,
                                PathFinder pathFinder, PathPoint point, IBlockAccess access)
    {
        int width = canGoFromToWidth();
        int height = canGoFromToHeight();
        Identity<?> fromId = fromHolder.owner();
        PermissionFlag attack = getPathFinderFlag(getClaim, from, fromHolder, pathFinder, point, access);

        for(int ix = -width; ix <= width; ix++)
            for(int iy = -height; iy <= height; iy++)
                for(int iz = -width; iz <= width; iz++)
                {
                    ClaimedChunk to = getClaim.apply((point.xCoord + ix) >> 4, (point.zCoord + iz) >> 4);
                    if(to == null)
                        return false;

                    FlagHolder toHolder = to.getFlagHolder(point.xCoord + ix, point.yCoord + iy, point.zCoord + iz);
                    if(!canGoFromTo(pathFinder, point, access, attack, from, to, fromHolder, toHolder))
                        return false;

                    Identity<?> toId = toHolder.owner();
                    if(fromId.equals(toId))
                        return true;
                }

        return true;
    }
}
