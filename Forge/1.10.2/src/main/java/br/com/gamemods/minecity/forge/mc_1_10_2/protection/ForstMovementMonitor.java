package br.com.gamemods.minecity.forge.mc_1_10_2.protection;

import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.forge.mc_1_10_2.MineCityFrost;
import br.com.gamemods.minecity.protection.MovementListener;
import br.com.gamemods.minecity.protection.MovementMonitor;
import net.minecraft.entity.Entity;

public class ForstMovementMonitor extends MovementMonitor<Entity, MineCityFrost>
{
    public ForstMovementMonitor(MineCityFrost server, Entity entity, BlockPos pos,
                                MovementListener<Entity, MineCityFrost> listener)
    {
        super(server, entity, pos, listener);
    }
}
