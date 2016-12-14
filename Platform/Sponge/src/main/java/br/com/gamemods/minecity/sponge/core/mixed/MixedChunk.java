package br.com.gamemods.minecity.sponge.core.mixed;

import br.com.gamemods.minecity.reactive.game.server.data.supplier.SupplierChunkData;
import br.com.gamemods.minecity.structure.ClaimedChunk;
import org.jetbrains.annotations.Nullable;

public interface MixedChunk extends SupplierChunkData
{
    @Nullable
    ClaimedChunk getClaimedChunk();

    void setClaimedChunk(@Nullable ClaimedChunk value);
}
