package br.com.gamemods.minecity.forge.mc_1_7_10.accessors.entity;

import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntity;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IPath;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IPathNavigate;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.mc_1_7_10.core.transformer.forge.SevenInterfaceTransformer;
import net.minecraft.entity.Entity;

@Referenced(at = SevenInterfaceTransformer.class)
public interface SevenPathNavigator extends IPathNavigate
{
    @Override
    default IPath getPathToEntity(IEntity entity)
    {
        return (IPath) getForgeNavigator().getPathToEntityLiving((Entity) entity);
    }
}
