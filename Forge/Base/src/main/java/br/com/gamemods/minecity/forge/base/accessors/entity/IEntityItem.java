package br.com.gamemods.minecity.forge.base.accessors.entity;

import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.ForgeInterfaceTransformer;
import net.minecraft.entity.item.EntityItem;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@Referenced(at = ForgeInterfaceTransformer.class)
public interface IEntityItem extends Pickable
{
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
    default IEntityLivingBase getEntityOwner()
    {
        String owner = getForgeEntity().getOwner();
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

    @Override
    default boolean isAllowedToPickup(PlayerID id)
    {
        return id.getName().equals(getForgeEntity().getOwner()) || Pickable.super.isAllowedToPickup(id);
    }

    default void setItemOwner(String name)
    {
        getForgeEntity().setOwner(name);
    }
}
