package br.com.gamemods.minecity.forge.base.protection.thaumcraft;

import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.forge.base.accessors.block.ITileEntity;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.ModInterfacesTransformer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@Referenced(at = ModInterfacesTransformer.class)
public interface ITileWarded extends ITileEntity
{
    default void setPlacedBy(UUID id)
    {
        getCustomData().setString("MC$O", id.toString());
        ((TileEntity) this).markDirty();
    }

    @Nullable
    default UUID getPlacedBy()
    {
        String placedBy = getCustomData().getString("MC$O");
        if(placedBy.isEmpty())
            return null;

        return UUID.fromString(placedBy);
    }

    default void setOwner(PlayerID identity)
    {
        setPlacedBy(identity.uniqueId);
        NBTTagCompound nbt = toNBT();
        String name = identity.getName();
        nbt.setString("owner", name);
        nbt.setInteger("oi", name.hashCode());
        ((TileEntity) this).readFromNBT(nbt);
        ((TileEntity) this).markDirty();
    }

    @Nullable
    default PlayerID getOwner()
    {
        UUID id = getPlacedBy();
        if(id == null)
            return null;

        return getIWorld().getServer().getPlayerId(id);
    }
}
