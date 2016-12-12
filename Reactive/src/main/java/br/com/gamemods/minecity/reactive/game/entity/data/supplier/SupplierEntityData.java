package br.com.gamemods.minecity.reactive.game.entity.data.supplier;

import br.com.gamemods.minecity.reactive.game.entity.data.EntityData;
import br.com.gamemods.minecity.reactive.game.server.data.ChunkData;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

@FunctionalInterface
public interface SupplierEntityData
{
    @NotNull
    EntityData getEntityData();

    @NotNull
    default Optional<ChunkData> getChunkData()
    {
        return getEntityData().getChunkData();
    }
}
