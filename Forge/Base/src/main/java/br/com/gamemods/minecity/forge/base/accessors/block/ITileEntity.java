package br.com.gamemods.minecity.forge.base.accessors.block;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public interface ITileEntity
{
    default TileEntity getForgeTile()
    {
        return (TileEntity) this;
    }

    default NBTTagCompound toNBT()
    {
        return ((TileEntity) this).serializeNBT();
    }
}
