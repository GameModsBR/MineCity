package br.com.gamemods.minecity.forge.base.accessors.entity.item;

import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityLivingBase;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.entity.projectile.EntityProjectile;
import br.com.gamemods.minecity.forge.base.accessors.entity.projectile.ProjectileShooter;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.ForgeInterfaceTransformer;
import net.minecraft.entity.item.EntityItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@Referenced(at = ForgeInterfaceTransformer.class)
public interface IEntityItem extends Pickable, EntityProjectile
{
    @Override
    default void detectShooter(MineCityForge mod)
    {
        String thrower = getItemThrower();
        if(thrower == null)
        {
            setShooter(new ProjectileShooter(getEntityPos(mod)));
            return;
        }

        IEntityPlayerMP player = getIWorld().getPlayerByName(thrower);
        if(player == null)
        {
            player = mod.server.getIPlayerList().getIPlayers().stream().filter(
                    p -> p.getEntityName().equals(thrower)).findFirst().orElse(null);
            if(player == null)
            {
                setShooter(new ProjectileShooter(getEntityPos(mod)));
                return;
            }
        }

        setShooter(new ProjectileShooter(getEntityPos(mod), player));
    }

    @Override
    default EntityItem getForgeEntity()
    {
        return (EntityItem) this;
    }

    default IItemStack getStack()
    {
        return (IItemStack) (Object) getForgeEntity().getEntityItem();
    }

    @Nullable
    @Override
    default PermissionFlag getPlayerAttackType()
    {
        return PermissionFlag.PICKUP;
    }

    @Nullable
    @Override
    default IEntityLivingBase getEntityOwner()
    {
        String owner = getItemOwner();
        //noinspection ConstantConditions
        if(owner == null)
            return null;

        return getIWorld().getPlayerByName(owner);
    }

    @Nullable
    @Override
    default UUID getEntityOwnerId()
    {
        IEntityLivingBase owner = getEntityOwner();
        if(owner == null)
            return null;

        return owner.getUniqueID();
    }

    default String getItemOwner()
    {
        return getForgeEntity().getOwner();
    }

    @Override
    default boolean isAllowedToPickup(PlayerID id)
    {
        return id.getName().equals(getItemOwner()) || Pickable.super.isAllowedToPickup(id);
    }

    default void setItemOwner(String name)
    {
        getForgeEntity().setOwner(name);
    }

    default int getPickupDelay()
    {
        return ((EntityItem) this).delayBeforeCanPickup;
    }

    default int getItemAge()
    {
        return ((EntityItem) this).age;
    }

    default String getItemThrower()
    {
        return ((EntityItem) this).getThrower();
    }

    @NotNull
    @Override
    default Type getType()
    {
        return Type.ITEM;
    }
}
