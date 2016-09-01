package br.com.gamemods.minecity.forge.base.protection;

import br.com.gamemods.minecity.api.world.WorldDim;
import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.accessors.block.IState;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntity;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityLivingBase;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.world.IWorldServer;
import br.com.gamemods.minecity.protection.MovementListener;

public interface ForgeMovementListener<E extends IEntity, F extends MineCityForge> extends MovementListener<E, F>
{
    @Override
    default boolean isSafeToStep(F server, E entity, WorldDim dim, int x, int y, int z)
    {
        IWorldServer world = (IWorldServer) server.world(dim);
        return world != null && isSafeToStep(entity, world.getIState(x, y, z));
    }

    default boolean isSafeToStep(E entity, IState state)
    {
        if(state.isOpaqueCube() || state.getIBlock().isLiquid())
            return true;

        if(entity instanceof IEntityLivingBase)
        {
            if(((IEntityLivingBase) entity).isElytraFlying())
                return true;

            if(entity instanceof IEntityPlayerMP && ((IEntityPlayerMP) entity).isFlying())
                return true;
        }

        return false;
    }
}
