package br.com.gamemods.minecity.forge.base.tile;

import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.forge.base.accessors.block.ITileEntity;
import net.minecraft.nbt.NBTTagCompound;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface ITileEntityData extends ITileEntity
{
    void setOwner(PlayerID id);
    PlayerID getOwner();

    default void load(@NotNull NBTTagCompound compound)
    {
        if(compound.hasKey("ownerId"))
            setOwner(new PlayerID(UUID.fromString(compound.getString("ownerId")), compound.getString("ownerName")));
    }

    default void save(NBTTagCompound compound)
    {
        PlayerID owner = getOwner();
        if(owner != null)
        {
            compound.setString("ownerId", owner.getUniqueId().toString());
            compound.setString("ownerName", owner.getName());
        }
    }
}
