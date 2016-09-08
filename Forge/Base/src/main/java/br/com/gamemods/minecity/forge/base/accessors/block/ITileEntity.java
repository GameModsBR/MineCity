package br.com.gamemods.minecity.forge.base.accessors.block;

import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.accessors.world.IWorldServer;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.ForgeInterfaceTransformer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

@Referenced(at = ForgeInterfaceTransformer.class)
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

    default IWorldServer getIWorld()
    {
        return (IWorldServer) ((TileEntity) this).getWorld();
    }

    default int getPosX()
    {
        return ((TileEntity) this).getPos().getX();
    }

    default int getPosY()
    {
        return ((TileEntity) this).getPos().getY();
    }

    default int getPosZ()
    {
        return ((TileEntity) this).getPos().getZ();
    }

    default BlockPos getBlockPos(MineCityForge mod)
    {
        return new BlockPos(mod.world(getIWorld()), getPosX(), getPosY(), getPosZ());
    }
}
