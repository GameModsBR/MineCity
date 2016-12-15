package br.com.gamemods.minecity.sponge.data.value;

import br.com.gamemods.minecity.api.world.EntityPos;
import br.com.gamemods.minecity.api.world.MinecraftEntity;
import br.com.gamemods.minecity.reactive.game.entity.data.EntityData;
import br.com.gamemods.minecity.reactive.game.server.data.ChunkData;
import br.com.gamemods.minecity.sponge.data.manipulator.reactive.SpongeManipulator;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.world.Chunk;

import java.util.Optional;

public class SpongeEntityData implements EntityData
{
    private final Entity entity;
    private final SpongeManipulator manipulator;
    private final MinecraftEntity minecraftEntity;

    public SpongeEntityData(SpongeManipulator manipulator, Entity entity)
    {
        this.entity = entity;
        this.manipulator = manipulator;
        minecraftEntity = new SpongeMinecraftEntity(
                manipulator.sponge, entity,
                manipulator.sponge.sender(entity, entity instanceof CommandSource? (CommandSource) entity : null)
        );
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

    @NotNull
    @Override
    public Optional<ChunkData> getChunkData()
    {
        Optional<Chunk> chunk = entity.getWorld().getChunk(entity.getLocation().getChunkPosition());
        if(!chunk.isPresent())
            return Optional.empty();

        return Optional.of(manipulator.server.getChunkData(chunk.get()));
    }

    public MinecraftEntity getMinecraftEntity()
    {
        return minecraftEntity;
    }
}
