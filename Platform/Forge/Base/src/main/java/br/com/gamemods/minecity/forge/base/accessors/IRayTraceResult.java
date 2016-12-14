package br.com.gamemods.minecity.forge.base.accessors;

import br.com.gamemods.minecity.api.shape.Point;
import br.com.gamemods.minecity.api.shape.PrecisePoint;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.api.world.WorldDim;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntity;

public interface IRayTraceResult
{
    BlockPos getHitBlockPos(WorldDim world);
    Point getHitBlockPos();
    PrecisePoint getEntityPos();
    IEntity getEntity();
    int getHitType();
    Direction getHitSide();
}
