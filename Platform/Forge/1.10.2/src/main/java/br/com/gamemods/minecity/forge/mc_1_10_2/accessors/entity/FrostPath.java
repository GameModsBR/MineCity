package br.com.gamemods.minecity.forge.mc_1_10_2.accessors.entity;

import br.com.gamemods.minecity.forge.base.accessors.entity.base.IPath;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IPathPoint;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.mc_1_10_2.core.transformer.forge.FrostInterfaceTransformer;
import net.minecraft.pathfinding.Path;

@Referenced(at = FrostInterfaceTransformer.class)
public interface FrostPath extends IPath
{
    default Path getForgePath()
    {
        return (Path) this;
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
