package br.com.gamemods.minecity.forge.base.accessors.entity;

import br.com.gamemods.minecity.api.PlayerID;
import net.minecraft.nbt.NBTTagCompound;

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
}
