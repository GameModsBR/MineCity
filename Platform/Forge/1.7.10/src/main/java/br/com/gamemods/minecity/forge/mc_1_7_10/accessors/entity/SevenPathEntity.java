package br.com.gamemods.minecity.forge.mc_1_7_10.accessors.entity;

import br.com.gamemods.minecity.forge.base.accessors.entity.base.IPath;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IPathPoint;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.mc_1_7_10.core.transformer.forge.SevenInterfaceTransformer;
import net.minecraft.pathfinding.PathEntity;

@Referenced(at = SevenInterfaceTransformer.class)
public interface SevenPathEntity extends IPath
{
    default PathEntity getForgePath()
    {
        return (PathEntity) this;
    }

    @Override
    default boolean isFinished()
    {
        return getForgePath().isFinished();
    }

    @Override
    default IPathPoint getFinalPoint()
    {
        return (IPathPoint) getForgePath().getFinalPathPoint();
    }
}
