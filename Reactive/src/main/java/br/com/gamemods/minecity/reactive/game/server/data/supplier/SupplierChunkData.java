package br.com.gamemods.minecity.reactive.game.server.data.supplier;

import br.com.gamemods.minecity.reactive.game.server.data.ChunkData;
import br.com.gamemods.minecity.reactive.game.server.data.WorldData;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface SupplierChunkData extends SupplierWorldData
{
    @NotNull
    ChunkData getChunkData();

    @NotNull
    @Override
    default WorldData getWorldData()
    {
        return getChunkData().getWorldData();
    }
}
