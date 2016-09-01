package br.com.gamemods.minecity.forge.mc_1_7_10.accessors;

import br.com.gamemods.minecity.api.shape.Point;
import br.com.gamemods.minecity.api.shape.PrecisePoint;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.accessors.IRayTraceResult;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntity;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.ForgeInterfaceTransformer;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

import java.util.NoSuchElementException;

@Referenced(at = ForgeInterfaceTransformer.class)
public interface SevenMovingObjectPosition extends IRayTraceResult
{
    @Override
    default Point getHitBlockPos()
    {
        MovingObjectPosition pos = (MovingObjectPosition) this;
        return new Point(pos.blockX, pos.blockY, pos.blockZ);
    }

    @Override
    default PrecisePoint getEntityPos()
    {
        Vec3 pos = ((MovingObjectPosition) this).hitVec;
        return new PrecisePoint(pos.xCoord, pos.yCoord, pos.zCoord);
    }

    @Override
    default IEntity getEntity()
    {
        return (IEntity) ((MovingObjectPosition) this).entityHit;
    }

    @Override
    default int getHitType()
    {
        return ((MovingObjectPosition) this).typeOfHit.ordinal();
    }

    @Override
    default Direction getHitSide()
    {
        int side = ((MovingObjectPosition) this).sideHit;
        switch(side)
        {
            case 0: return Direction.DOWN;
            case 1: return Direction.UP;
            case 2: return Direction.EAST;
            case 3: return Direction.WEST;
            case 4: return Direction.NORTH;
            case 5: return Direction.SOUTH;
            default: throw new NoSuchElementException("sideHit = "+side);
        }
    }
}
