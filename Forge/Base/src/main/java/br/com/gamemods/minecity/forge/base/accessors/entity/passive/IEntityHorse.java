package br.com.gamemods.minecity.forge.base.accessors.entity.passive;

import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityLivingBase;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.command.ForgePlayer;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.ForgeInterfaceTransformer;
import br.com.gamemods.minecity.forge.base.protection.reaction.NoReaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.Reaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.SingleBlockReaction;
import net.minecraft.entity.passive.EntityHorse;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@Referenced(at = ForgeInterfaceTransformer.class)
public interface IEntityHorse extends IEntityAnimal
{
    @Override
    default EntityHorse getForgeEntity()
    {
        return (EntityHorse) this;
    }

    @Nullable
    @Override
    default UUID getEntityOwnerId()
    {
        return ((EntityHorse) this).getOwnerUniqueId();
    }

    @Nullable
    @Override
    default IEntityLivingBase getEntityOwner()
    {
        EntityHorse horse = (EntityHorse) this;
        UUID uuid = horse.getOwnerUniqueId();
        if(uuid == null)
            return null;

        return (IEntityLivingBase) horse.worldObj.getPlayerEntityByUUID(uuid);
    }

    @Override
    default Reaction reactPlayerInteraction(ForgePlayer<?, ?, ?> player, IItemStack stack, boolean offHand)
    {
        if(isChild() || player.getUniqueId().equals(getEntityOwnerId()))
            return NoReaction.INSTANCE;

        if(stack != null && !isChild())
        {
            if(canHaveChest() && !isCarryingChest() && stack.getIItem().getUnlocalizedName().equals("tile.chest"))
            {
                SingleBlockReaction react = new SingleBlockReaction(getBlockPos(player.getServer()),PermissionFlag.MODIFY);
                react.addDenialListener((reaction, permissible, flag, pos, message) -> {
                    setCarryingChest(true);
                    setCarryingChest(false);
                });
            }

            if(isBreedingItem(stack))
                return new SingleBlockReaction(getBlockPos(player.getServer()), PermissionFlag.PVC);
        }

        return new SingleBlockReaction(getBlockPos(player.getServer()), PermissionFlag.RIDE);
    }

    default void setCarryingChest(boolean val)
    {
        ((EntityHorse) this).setChested(val);
    }

    default boolean canHaveChest()
    {
        return ((EntityHorse) this).getType().canBeChested();
    }

    default boolean isCarryingChest()
    {
        return ((EntityHorse) this).isChested();
    }
}
