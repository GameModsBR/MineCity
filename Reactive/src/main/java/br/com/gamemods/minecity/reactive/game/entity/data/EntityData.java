package br.com.gamemods.minecity.reactive.game.entity.data;

import br.com.gamemods.minecity.api.world.EntityPos;
import br.com.gamemods.minecity.reactive.game.server.data.ChunkData;
import br.com.gamemods.minecity.reactive.game.server.data.ServerManipulator;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface EntityData
{
    Object getEntity();

    /**
     * The current entity position
     * @return the associated WorldDim must contains an object that is accepted by {@link ServerManipulator#getWorldData(Object)}
     */
    EntityPos getEntityPosition();

    /**
     * The chunk where this entity is currently located
     */
    @NotNull
    Optional<ChunkData> getChunkData();
}
