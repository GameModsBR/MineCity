package br.com.gamemods.minecity.forge.base.accessors.entity.base;

import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.ForgeInterfaceTransformer;
import net.minecraft.entity.Entity;
import net.minecraft.pathfinding.PathNavigate;

@Referenced(at = ForgeInterfaceTransformer.class)
public interface IPathNavigate
{
    default PathNavigate getForgeNavigator()
    {
        return (PathNavigate) this;
    }

    default boolean noPath()
    {
        return getForgeNavigator().noPath();
    }

    default IPath getPathToEntity(IEntity entity)
    {
        return (IPath) getForgeNavigator().getPathToEntityLiving((Entity) entity);
    }
}
