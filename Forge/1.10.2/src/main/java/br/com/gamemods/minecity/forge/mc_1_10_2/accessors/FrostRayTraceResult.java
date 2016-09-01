package br.com.gamemods.minecity.forge.mc_1_10_2.accessors;

import br.com.gamemods.minecity.api.shape.Point;
import br.com.gamemods.minecity.api.shape.PrecisePoint;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.accessors.IRayTraceResult;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntity;
import br.com.gamemods.minecity.forge.mc_1_10_2.FrostUtil;
import br.com.gamemods.minecity.forge.mc_1_10_2.core.transformer.forge.FrostInterfaceTransformer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

@Referenced(at = FrostInterfaceTransformer.class)
public interface FrostRayTraceResult extends IRayTraceResult
{
    @Override
    default Point getHitBlockPos()
    {
        BlockPos pos = ((RayTraceResult) this).getBlockPos();
        return new Point(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    default PrecisePoint getEntityPos()
    {
        Vec3d pos = ((RayTraceResult) this).hitVec;
        return new PrecisePoint(pos.xCoord, pos.yCoord, pos.zCoord);
    }

    @Override
    default IEntity getEntity()
    {
        return (IEntity) ((RayTraceResult) this).entityHit;
    }

    @Override
    default int getHitType()
    {
        return ((RayTraceResult) this).typeOfHit.ordinal();
    }

    @Override
    default Direction getHitSide()
    {
        return FrostUtil.toDirection(((RayTraceResult) this).sideHit);
    }
}
