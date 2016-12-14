package br.com.gamemods.minecity.reactive.game.block;

import br.com.gamemods.minecity.api.shape.Point;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.reactive.game.block.data.BlockStateData;
import br.com.gamemods.minecity.reactive.game.block.data.BlockTypeData;
import br.com.gamemods.minecity.reactive.game.block.data.TileEntityData;
import br.com.gamemods.minecity.reactive.game.entity.ReactiveEntity;
import br.com.gamemods.minecity.reactive.game.entity.data.Hand;
import br.com.gamemods.minecity.reactive.game.item.ReactiveItemStack;
import br.com.gamemods.minecity.reactive.game.server.data.ChunkData;
import br.com.gamemods.minecity.reactive.reaction.InteractReaction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * A block with all its properties ready for reaction.
 */
public final class ReactiveBlock
{
    @NotNull
    private final ChunkData chunk;

    @NotNull
    private final BlockPos pos;

    @NotNull
    private final BlockStateData state;

    @Nullable
    private final TileEntityData tileEntity;

    public ReactiveBlock(ChunkData chunk, BlockPos pos, BlockStateData state)
    {
        if(!chunk.getChunkPos().equals(pos.getChunk()))
            throw new IllegalArgumentException("chunk.chunkPos != pos.chunk");
        this.chunk = chunk;
        this.pos = pos;
        this.state = state;
        this.tileEntity = chunk.getTileEntityData(pos).orElse(null);
    }

    @NotNull
    public InteractReaction rightClick(ReactiveEntity entity, Hand hand, ReactiveItemStack stack, Direction face, Point point)
    {
        InteractReaction reaction = new InteractReaction();
        propertyStream().forEachOrdered(prop-> prop.reactRightClick(reaction, entity, hand, stack, this, face, point));
        return reaction;
    }

    @NotNull
    public InteractReaction leftClick(ReactiveEntity entity, Hand hand, ReactiveItemStack stack, Direction face, Point point)
    {
        InteractReaction reaction = new InteractReaction();
        propertyStream().forEachOrdered(prop-> prop.reactLeftClick(reaction, entity, hand, stack, this, face, point));
        return reaction;
    }

    @NotNull
    public Stream<ReactiveBlockProperty> propertyStream()
    {
        return Stream.concat(
                Stream.of(state.getBlockTypeData().getReactiveBlockType().orElse(null), state.getReactiveBlockState().orElse(null)),
                Stream.concat(
                        state.reactiveTraitStream(),
                        Optional.ofNullable(tileEntity)
                                .flatMap(TileEntityData::getReactiveTileEntity)
                                .map(Stream::of).orElse(Stream.empty())
                )
        ).filter(prop-> prop != null);
    }

    @NotNull
    public ChunkData getChunk()
    {
        return chunk;
    }

    @NotNull
    public BlockPos getPosition()
    {
        return pos;
    }

    @NotNull
    public BlockStateData getState()
    {
        return state;
    }

    @NotNull
    public BlockTypeData getType()
    {
        return state.getBlockStateData().getBlockTypeData();
    }

    @NotNull
    public Optional<TileEntityData> getTileEntity()
    {
        return Optional.ofNullable(tileEntity);
    }

    @Override
    public boolean equals(Object o)
    {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;

        ReactiveBlock that = (ReactiveBlock) o;
        return chunk.equals(that.chunk)
                && pos.equals(that.pos)
                && state.equals(that.state)
                && (tileEntity != null ? tileEntity.equals(that.tileEntity) : that.tileEntity == null)
        ;
    }

    @Override
    public int hashCode()
    {
        int result = chunk.hashCode();
        result = 31 * result+pos.hashCode();
        result = 31 * result+state.hashCode();
        result = 31 * result+(tileEntity != null ? tileEntity.hashCode() : 0);
        return result;
    }

    @NotNull
    @Override
    public String toString()
    {
        return "ReactiveBlock{"+
                "chunk="+chunk+
                ", pos="+pos+
                ", state="+state+
                ", tileEntity="+tileEntity+
                '}';
    }
}
