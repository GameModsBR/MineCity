package br.com.gamemods.minecity.sponge.listeners;

import br.com.gamemods.minecity.api.world.WorldDim;
import br.com.gamemods.minecity.protection.MovementListener;
import br.com.gamemods.minecity.sponge.MineCitySponge;
import br.com.gamemods.minecity.sponge.core.mixed.MixedEntityLivingBase;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.property.AbstractProperty;
import org.spongepowered.api.data.property.block.SolidCubeProperty;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.extra.fluid.data.property.FluidViscosityProperty;
import org.spongepowered.api.world.World;

import java.util.Optional;

public interface CommonMovimentListener<E extends Entity> extends MovementListener<E, MineCitySponge>
{
    @Override
    default boolean isSafeToStep(MineCitySponge server, E entity, WorldDim dim, int x, int y, int z)
    {
        Optional<World> world = server.world(dim);
        return world.isPresent() && isSafeToStep(entity, world.get().getBlock(x, y, z));
    }

    default boolean isSafeToStep(E entity, BlockState state)
    {
        if(state.getProperty(SolidCubeProperty.class).map(AbstractProperty::getValue).orElse(false)
                || state.getProperty(FluidViscosityProperty.class).isPresent())
            return true;

        if(entity instanceof Living)
        {
            if(entity instanceof MixedEntityLivingBase && ((MixedEntityLivingBase) entity).isFlyingOnElytra())
                return true;

            if(entity instanceof Player && entity.get(Keys.IS_FLYING).orElse(false))
                return true;
        }

        return false;
    }
}
