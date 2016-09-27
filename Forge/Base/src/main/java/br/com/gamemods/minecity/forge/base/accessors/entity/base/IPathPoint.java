package br.com.gamemods.minecity.forge.base.accessors.entity.base;

import br.com.gamemods.minecity.api.shape.Point;
import br.com.gamemods.minecity.api.shape.PrecisePoint;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.ForgeInterfaceTransformer;
import net.minecraft.pathfinding.PathPoint;

@Referenced(at = ForgeInterfaceTransformer.class)
public interface IPathPoint
{
    default PathPoint getForgePoint()
    {
        return (PathPoint) this;
    }

    default float getDistanceToTarget()
    {
        return getForgePoint().distanceToTarget;
    }

    default Point toPoint()
    {
        PathPoint p = (PathPoint) this;
        return new Point(p.xCoord, p.yCoord, p.zCoord);
    }

    default PrecisePoint toPrecisePoint()
    {
        PathPoint p = (PathPoint) this;
        return new PrecisePoint(p.xCoord+0.5, p.yCoord+0.5, p.zCoord+0.5);
    }
}
