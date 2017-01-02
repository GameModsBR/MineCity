package br.com.gamemods.minecity.reactive.game.block;

import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.reactive.game.block.data.BlockSnapshotData;
import br.com.gamemods.minecity.reactive.game.block.data.BlockStateData;
import br.com.gamemods.minecity.reactive.game.block.data.TileEntityData;
import br.com.gamemods.minecity.reactive.game.block.data.supplier.SupplierBlockStateData;
import br.com.gamemods.minecity.reactive.game.server.data.ChunkData;
import br.com.gamemods.minecity.reactive.game.server.data.supplier.SupplierChunkData;
import br.com.gamemods.minecity.reactive.reaction.NoReaction;
import br.com.gamemods.minecity.reactive.reaction.Reaction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * A block with all its properties ready for reaction.
 */
public final class ReactiveBlock implements SupplierBlockStateData, SupplierChunkData
{
    @NotNull
    private final ChunkData chunk;

    @NotNull
    private final BlockSnapshotData snapshot;

    @Nullable
    private final TileEntityData tileEntity;

    public ReactiveBlock(ChunkData chunk, BlockSnapshotData snapshot)
    {
        if(!chunk.getChunkPos().equals(snapshot.getPosition().getChunk()))
            throw new IllegalArgumentException("chunk.chunkPos != snapshot.position.chunk");

        this.chunk = chunk;
        this.snapshot = snapshot;
        tileEntity = chunk.getTileEntityData(snapshot.getPosition()).orElse(null);
    }

    @NotNull
    public Reaction rightClick(Interaction event)
    {
        if(event.getBlock() != this) throw new IllegalArgumentException(event.getBlock()+" != "+this);

        return propertyStream()
                .map(prop -> prop.reactRightClick(event))
                .reduce(Reaction::combine)
                .orElse(NoReaction.INSTANCE);
    }

    @NotNull
    public Reaction leftClick(Interaction event)
    {
        if(event.getBlock() != this) throw new IllegalArgumentException(event.getBlock()+" != "+this);

        return propertyStream().map(prop -> prop.reactLeftClick(event))
                .reduce(Reaction::combine)
                .orElse(NoReaction.INSTANCE);
    }

    @NotNull
    public Stream<ReactiveBlockProperty> propertyStream()
    {
        return Stream.concat(
                Stream.of(
                        snapshot.getBlockTypeData().getReactiveBlockType().orElse(null),
                        snapshot.getBlockStateData().getReactiveBlockState().orElse(null)
                ),
                Stream.concat(
                        snapshot.getBlockStateData().reactiveBlockTraitStream(),
                        snapshot.getTileEntityData()
                                .flatMap(TileEntityData::getReactiveTileEntity)
                                .map(Stream::of).orElse(Stream.empty())
                )
        ).filter(Objects::nonNull);
    }

    @Override
    @NotNull
    public ChunkData getChunkData()
    {
        return chunk;
    }

    @NotNull
    public BlockPos getPosition()
    {
        return snapshot.getPosition();
    }

    @NotNull
    public Optional<TileEntityData> getTileEntity()
    {
        return Optional.ofNullable(tileEntity);
    }

    @NotNull
    @Override
    public BlockStateData getBlockStateData()
    {
        return snapshot.getBlockStateData();
    }

    @Override
    public boolean equals(Object o)
    {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;

        ReactiveBlock that = (ReactiveBlock) o;

        return chunk.equals(that.chunk) &&
                snapshot.equals(that.snapshot) &&
                (tileEntity != null ? tileEntity.equals(that.tileEntity) : that.tileEntity == null);
    }

    @Override
    public int hashCode()
    {
        int result = chunk.hashCode();
        result = 31 * result+snapshot.hashCode();
        result = 31 * result+(tileEntity != null ? tileEntity.hashCode() : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return "ReactiveBlock{"+
                "chunk="+chunk+
                ", snapshot="+snapshot+
                ", tileEntity="+tileEntity+
                '}';
    }
}
