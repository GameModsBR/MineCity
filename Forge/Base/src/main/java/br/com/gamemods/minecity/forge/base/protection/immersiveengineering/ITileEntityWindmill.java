package br.com.gamemods.minecity.forge.base.protection.immersiveengineering;

import br.com.gamemods.minecity.api.shape.Cuboid;
import br.com.gamemods.minecity.api.shape.Point;
import br.com.gamemods.minecity.api.shape.Shape;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.forge.base.ForgeUtil;
import br.com.gamemods.minecity.forge.base.accessors.block.ITileEntity;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.ModInterfacesTransformer;
import net.minecraft.nbt.NBTTagCompound;

@Referenced(at = ModInterfacesTransformer.class)
public interface ITileEntityWindmill extends ITileEntity, Shaped
{
    @Override
    default Shape getShape()
    {
        NBTTagCompound nbt = toNBT();
        Direction facing = ForgeUtil.toDirection(nbt.getInteger("facing")).right();
        Point pos = getBlockPos();

        return new Cuboid(pos.add(facing.x * 6 , 6, facing.z * 6), pos.subtract(facing.x * 6, 6, facing.z * 6));
    }
}
