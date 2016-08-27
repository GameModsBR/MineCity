package br.com.gamemods.minecity.forge.base.accessors;

import br.com.gamemods.minecity.api.shape.Point;
import br.com.gamemods.minecity.api.shape.PrecisePoint;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.forge.base.accessors.entity.IEntity;

public interface IRayTraceResult
{
    Point getHitBlockPos();
    PrecisePoint getEntityPos();
    IEntity getEntity();
    int getHitType();
    Direction getHitSide();
}
