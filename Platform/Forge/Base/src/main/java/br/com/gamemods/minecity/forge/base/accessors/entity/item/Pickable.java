package br.com.gamemods.minecity.forge.base.accessors.entity.item;

import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntity;
import net.minecraft.nbt.NBTTagCompound;
import org.jetbrains.annotations.NotNull;

public interface Pickable extends IEntity
{
    default void allowToPickup(PlayerID id)
    {
        NBTTagCompound nbt = getForgeEntity().getEntityData();
        NBTTagCompound allow = nbt.getCompoundTag("MineCityAllowPickup");
        if(allow.hasNoTags())
            nbt.setTag("MineCityAllowPickup", allow);

        allow.setString(id.getUniqueId().toString(), id.getName());
    }

    default boolean isAllowedToPickup(PlayerID id)
    {
        NBTTagCompound nbt = getForgeEntity().getEntityData();
        return nbt.hasKey("MineCityAllowPickup") &&
                nbt.getCompoundTag("MineCityAllowPickup").hasKey(id.getUniqueId().toString());
    }

    @NotNull
    @Override
    default Type getType()
    {
        return Type.ITEM;
    }
}
