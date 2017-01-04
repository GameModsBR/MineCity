package br.com.gamemods.minecity.sponge.data.value;

import br.com.gamemods.minecity.api.world.EntityPos;
import br.com.gamemods.minecity.reactive.game.entity.data.EntityData;
import br.com.gamemods.minecity.reactive.game.server.data.ChunkData;
import br.com.gamemods.minecity.sponge.data.manipulator.reactive.SpongeManipulator;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.data.manipulator.mutable.entity.FoodData;
import org.spongepowered.api.effect.Viewer;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.Chunk;

import java.util.Optional;

public class SpongeEntityData implements EntityData
{
    private final Entity entity;
    private final SpongeManipulator manipulator;

    public SpongeEntityData(SpongeManipulator manipulator, Entity entity)
    {
        this.entity = entity;
        this.manipulator = manipulator;
    }

    @Override
    public Entity getEntity()
    {
        return entity;
    }

    @Override
    public EntityPos getEntityPosition()
    {
        return manipulator.sponge.entityPos(entity);
    }

    @Override
    public boolean sendBlockUpdate(int x, int y, int z)
    {
        if(entity instanceof Viewer)
        {
            ((Viewer) entity).sendBlockChange(x, y, z, entity.getWorld().getBlock(x, y, z));
            return true;
        }

        return false;
    }

    @Override
    public boolean sendHungerUpdate()
    {
        if(entity instanceof Player)
        {
            Player player = (Player) entity;
            FoodData data = player.getFoodData();
            double exhaustion = data.exhaustion().get();
            int foodLevel = data.foodLevel().get();
            double saturation = data.saturation().get();

            data.exhaustion().set(exhaustion-1 >= 0? exhaustion+1 : exhaustion-1);
            data.foodLevel().set(foodLevel-1 >= 0? foodLevel+1 : foodLevel-1);
            data.saturation().set(saturation-1 >=0 ? saturation+1 : saturation-1);

            data.exhaustion().set(exhaustion);
            data.foodLevel().set(foodLevel);
            data.saturation().set(saturation);
            return true;
        }

        return false;
    }

    @NotNull
    @Override
    public Optional<ChunkData> getChunkData()
    {
        Optional<Chunk> chunk = entity.getWorld().getChunk(entity.getLocation().getChunkPosition());
        if(!chunk.isPresent())
            return Optional.empty();

        return Optional.of(manipulator.server.getChunkData(chunk.get()));
    }

    @Override
    public String toString()
    {
        return "SpongeEntityData{"+
                "entity="+entity+
                '}';
    }
}
