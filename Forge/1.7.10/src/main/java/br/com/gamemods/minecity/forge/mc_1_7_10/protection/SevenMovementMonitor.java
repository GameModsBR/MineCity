package br.com.gamemods.minecity.forge.mc_1_7_10.protection;

import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.forge.mc_1_7_10.MineCitySeven;
import br.com.gamemods.minecity.protection.MovementListener;
import br.com.gamemods.minecity.protection.MovementMonitor;
import net.minecraft.entity.Entity;

public class SevenMovementMonitor extends MovementMonitor<Entity, MineCitySeven>
{
    public SevenMovementMonitor(MineCitySeven server, Entity entity,
                                BlockPos pos,
                                MovementListener<Entity, MineCitySeven> listener)
    {
        super(server, entity, pos, listener);
    }
}
