package br.com.gamemods.minecity.forge.base.accessors.entity.passive;

import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityLivingBase;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.command.ForgePlayer;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.ForgeInterfaceTransformer;
import br.com.gamemods.minecity.reactive.reaction.NoReaction;
import br.com.gamemods.minecity.reactive.reaction.Reaction;
import br.com.gamemods.minecity.reactive.reaction.SingleBlockReaction;
import net.minecraft.entity.passive.EntityTameable;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@Referenced(at = ForgeInterfaceTransformer.class)
public interface IEntityTameable extends IEntityAnimal
{
    @Override
    default EntityTameable getForgeEntity()
    {
        return (EntityTameable) this;
    }

    default boolean isTamed()
    {
        return getForgeEntity().isTamed();
    }
    
    default boolean isSitting()
    {
        return getForgeEntity().isSitting();
    }

    @Nullable
    @Override
    default UUID getEntityOwnerId()
    {
        return ((EntityTameable) this).getOwnerId();
    }

    @Nullable
    @Override
    default IEntityLivingBase getEntityOwner()
    {
        return (IEntityLivingBase) ((EntityTameable) this).getOwner();
    }

    @Nullable
    @Override
    default PermissionFlag getPlayerAttackType()
    {
        if(isTamed())
            return PermissionFlag.PVP;
        
        if(isNamed())
            return PermissionFlag.MODIFY;
        
        return PermissionFlag.PVC;
    }

    @Override
    default Reaction reactPlayerInteractLiving(ForgePlayer<?, ?, ?> player, IItemStack stack, boolean offHand)
    {
        if(isTamed() && !isSitting() && player.getEntityUUID().equals(getEntityOwnerId()))
            return new SingleBlockReaction(getBlockPos(player.getServer()), PermissionFlag.MODIFY);


        switch(getType())
        {
            case STORAGE: return new SingleBlockReaction(getBlockPos(player.getServer()), PermissionFlag.OPEN);
            case VEHICLE: return new SingleBlockReaction(getBlockPos(player.getServer()), PermissionFlag.RIDE);
            default: return NoReaction.INSTANCE;
        }
    }
}
