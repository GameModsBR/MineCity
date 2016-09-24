package br.com.gamemods.minecity.forge.mc_1_7_10.tileentity;

import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.forge.base.tile.ITileEntityData;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import org.jetbrains.annotations.NotNull;

public class SevenTileEntityData extends TileEntity implements ITileEntityData
{
    private PlayerID owner;

    @Override
    public void setOwner(PlayerID owner)
    {
        this.owner = owner;
    }

    @Override
    public PlayerID getOwner()
    {
        return owner;
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);
        save(nbt);
    }

    @Override
    public void readFromNBT(@NotNull NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);
        load(nbt);
    }
}
