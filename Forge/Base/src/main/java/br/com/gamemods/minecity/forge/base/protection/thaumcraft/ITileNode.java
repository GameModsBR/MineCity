package br.com.gamemods.minecity.forge.base.protection.thaumcraft;

import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.forge.base.accessors.block.ITileEntity;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.ModInterfacesTransformer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

import java.util.UUID;

@Referenced(at = ModInterfacesTransformer.class)
public interface ITileNode extends ITileEntity
{
    default void setOwner(PlayerID id)
    {
        NBTTagCompound nbt = getCustomData();
        nbt.setString("mineCityOwnerID", id.getUniqueId().toString());
        nbt.setString("mineCityOwnerName", id.getName());
        ((TileEntity) this).markDirty();
    }

    default PlayerID getOwner()
    {
        NBTTagCompound nbt = getCustomData();
        if(!nbt.hasKey("mineCityOwnerID"))
            return null;

        return new PlayerID(UUID.fromString(nbt.getString("mineCityOwnerID")), nbt.getString("mineCityOwnerName"));
    }
}
