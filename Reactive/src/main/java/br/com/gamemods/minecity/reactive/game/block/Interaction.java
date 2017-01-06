package br.com.gamemods.minecity.reactive.game.block;

import br.com.gamemods.minecity.api.shape.PrecisePoint;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.reactive.game.block.data.BlockStateData;
import br.com.gamemods.minecity.reactive.game.block.data.supplier.SupplierBlockStateData;
import br.com.gamemods.minecity.reactive.game.entity.data.EntityData;
import br.com.gamemods.minecity.reactive.game.entity.data.Hand;
import br.com.gamemods.minecity.reactive.game.item.ReactiveItemStack;
import br.com.gamemods.minecity.reactive.game.server.data.ChunkData;
import br.com.gamemods.minecity.reactive.game.server.data.supplier.SupplierChunkData;
import br.com.gamemods.minecity.reactive.reaction.InteractReaction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public final class Interaction implements SupplierBlockStateData, SupplierChunkData
{
    private final Click click;
    private final EntityData entity;
    private final Hand hand;
    private final ReactiveItemStack itemStack;
    private final ReactiveBlock block;
    @Nullable
    private final Direction blockFace;
    private final PrecisePoint clickPoint;
    private InteractReaction reactionItemFirst = new InteractReaction();
    private InteractReaction reactionBlock = new InteractReaction();
    private InteractReaction reactionItemLast = new InteractReaction();

    public Interaction(Click click, EntityData entity, Hand hand, ReactiveItemStack itemStack,
                       ReactiveBlock block, @Nullable Direction blockFace, PrecisePoint clickPoint)
    {
        this.click = click;
        this.entity = entity;
        this.hand = hand;
        this.itemStack = itemStack;
        this.block = block;
        this.blockFace = blockFace;
        this.clickPoint = clickPoint;
    }

    @NotNull
    @Override
    public BlockStateData getBlockStateData()
    {
        return block.getBlockStateData();
    }

    @NotNull
    @Override
    public ChunkData getChunkData()
    {
        return block.getChunkData();
    }

    public InteractReaction result()
    {
        InteractReaction result = new InteractReaction();
        Stream.of(reactionItemFirst, reactionBlock, reactionItemLast)
                .filter(Objects::nonNull)
                .forEachOrdered(result::combine);
        return result;
    }

    public void setReactionItemFirst(@NotNull InteractReaction reactionItemFirst)
    {
        this.reactionItemFirst = reactionItemFirst;
    }

    public void setReactionBlock(@NotNull InteractReaction reactionBlock)
    {
        this.reactionBlock = reactionBlock;
    }

    public void setReactionItemLast(@NotNull InteractReaction reactionItemLast)
    {
        this.reactionItemLast = reactionItemLast;
    }

    public Click getClick()
    {
        return click;
    }

    public EntityData getEntity()
    {
        return entity;
    }

    public Hand getHand()
    {
        return hand;
    }

    public ReactiveItemStack getStack()
    {
        return itemStack;
    }

    public ReactiveBlock getBlock()
    {
        return block;
    }

    public BlockStateData getBlockState()
    {
        return getBlock().getBlockStateData();
    }

    public Optional<Direction> getBlockFace()
    {
        return Optional.ofNullable(blockFace);
    }

    public PrecisePoint getClickPoint()
    {
        return clickPoint;
    }

    public InteractReaction getReactionItemFirst()
    {
        return reactionItemFirst;
    }

    public InteractReaction getReactionBlock()
    {
        return reactionBlock;
    }

    public InteractReaction getReactionItemLast()
    {
        return reactionItemLast;
    }

    public enum Click
    {
        RIGHT, LEFT
    }
}
