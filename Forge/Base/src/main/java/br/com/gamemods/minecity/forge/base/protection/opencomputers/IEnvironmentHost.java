package br.com.gamemods.minecity.forge.base.protection.opencomputers;

import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.EntityPos;
import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.ModInterfacesTransformer;
import net.minecraft.world.World;

@Referenced(at = ModInterfacesTransformer.class)
public interface IEnvironmentHost
{
    World world();
    double xPosition();
    double yPosition();
    double zPosition();

    default BlockPos envBlockPos(MineCityForge mod)
    {
        return new BlockPos(mod.world(world()), (int)xPosition(), (int)yPosition(), (int)zPosition());
    }

    default EntityPos envEntityPos(MineCityForge mod)
    {
        return new EntityPos(mod.world(world()), xPosition(), yPosition(), zPosition(), 0, 0);
    }
}
