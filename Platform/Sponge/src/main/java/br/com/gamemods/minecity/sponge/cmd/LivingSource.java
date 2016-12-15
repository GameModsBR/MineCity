package br.com.gamemods.minecity.sponge.cmd;

import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.sponge.MineCitySponge;
import com.flowpowered.math.vector.Vector3d;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.Living;

public class LivingSource<Entity extends Living, Source extends CommandSource> extends EntitySource<Entity, Source>
{
    public LivingSource(MineCitySponge server, Source souce, Entity entity)
    {
        super(server, souce, entity);
    }

    @Override
    public Direction getCardinalDirection()
    {
        Vector3d headRotation = subject.getHeadRotation();
        float yaw = (float) headRotation.getY();
        double d = (double)((yaw + 180.0F)*8.0F/360.0F) + 0.5D;
        int i = (int) d;
        return Direction.cardinal8.get((d < (double)i ? i - 1 : i) & 7);
    }
}
