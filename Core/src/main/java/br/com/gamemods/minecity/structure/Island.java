package br.com.gamemods.minecity.structure;

import br.com.gamemods.minecity.api.WorldDim;
import org.jetbrains.annotations.NotNull;

public interface Island extends ChunkOwner
{
    int getId();

    @NotNull
    WorldDim getWorld();
    @NotNull
    City getCity();
    int getSizeX();
    int getSizeZ();
    int getChunkCount();
}
