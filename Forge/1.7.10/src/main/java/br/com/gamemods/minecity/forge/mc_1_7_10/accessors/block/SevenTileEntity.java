package br.com.gamemods.minecity.forge.mc_1_7_10.accessors.block;

import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.accessors.block.ITileEntity;
import br.com.gamemods.minecity.forge.base.accessors.world.IWorldServer;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.mc_1_7_10.core.transformer.forge.SevenInterfaceTransformer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

@Referenced(at = SevenInterfaceTransformer.class)
public interface SevenTileEntity extends ITileEntity
{
    @Override
    default NBTTagCompound toNBT()
    {
        NBTTagCompound nbt = new NBTTagCompound();
        ((TileEntity) this).writeToNBT(nbt);
        return nbt;
    }

    @Override
    default int getPosX()
    {
        return ((TileEntity) this).xCoord;
    }

    @Override
    default int getPosY()
    {
        return ((TileEntity) this).yCoord;
    }

    @Override
    default int getPosZ()
    {
        return ((TileEntity) this).zCoord;
    }

    @Override
    default IWorldServer getIWorld()
    {
        return (IWorldServer) ((TileEntity) this).getWorldObj();
    }

    @Override
    default BlockPos getBlockPos(MineCityForge mod)
    {
        TileEntity tile = (TileEntity) this;
        return new BlockPos(mod.world(tile.getWorldObj()), tile.xCoord, tile.yCoord, tile.zCoord);
    }
}
