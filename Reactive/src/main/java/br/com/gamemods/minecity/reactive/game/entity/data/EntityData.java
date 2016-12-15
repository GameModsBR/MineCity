package br.com.gamemods.minecity.reactive.game.entity.data;

import br.com.gamemods.minecity.api.shape.PrecisePoint;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.api.world.EntityPos;
import br.com.gamemods.minecity.reactive.game.block.InteractEvent;
import br.com.gamemods.minecity.reactive.game.block.ReactiveBlock;
import br.com.gamemods.minecity.reactive.game.item.ReactiveItemStack;
import br.com.gamemods.minecity.reactive.game.server.data.ChunkData;
import br.com.gamemods.minecity.reactive.game.server.data.ServerManipulator;
import br.com.gamemods.minecity.reactive.reaction.InteractReaction;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface EntityData
{
    Object getEntity();

    /**
     * The current entity position
     *
     * @return the associated WorldDim must contains an object that is accepted by {@link ServerManipulator#getWorldData(Object)}
     */
    EntityPos getEntityPosition();

    /**
     * The chunk where this entity is currently located
     */
    @NotNull
    Optional<ChunkData> getChunkData();

    default InteractReaction onRightClick(Hand hand, ReactiveItemStack stack, ReactiveBlock block, Direction side,
                                          PrecisePoint point)
    {
        InteractEvent event = new InteractEvent(InteractEvent.Click.RIGHT, this, hand, stack, block, side, point);

        event.getReactionItemFirst().combineAction(stack.rightClickFirst(event));
        event.getReactionBlock().combineBlock(block.rightClick(event));
        event.getReactionItemLast().combineItem(stack.rightClickFirst(event));

        return event.result();
    }
}
