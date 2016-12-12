package br.com.gamemods.minecity.sponge.core.mixin;

import br.com.gamemods.minecity.reactive.ReactiveLayer;
import br.com.gamemods.minecity.reactive.game.server.data.ChunkData;
import br.com.gamemods.minecity.sponge.core.mixed.MixedChunk;
import br.com.gamemods.minecity.structure.ClaimedChunk;
import net.minecraft.world.chunk.Chunk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Chunk.class)
public abstract class MixinChunk implements MixedChunk
{
    @Nullable
    private ChunkData chunkData;

    @Nullable
    private ClaimedChunk claimedChunk;

    @NotNull
    @Override
    public ChunkData getChunkData()
    {
        if(chunkData != null)
            return chunkData;

        return chunkData = ReactiveLayer.getServerManipulator().getChunkData(this).get();
    }

    @Nullable
    @Override
    public ClaimedChunk getClaimedChunk()
    {
        return claimedChunk;
    }

    @Override
    public void setClaimedChunk(@Nullable ClaimedChunk claimedChunk)
    {
        this.claimedChunk = claimedChunk;
    }
}
