package br.com.gamemods.minecity.reactive.game.entity.data;

import br.com.gamemods.minecity.api.shape.Point;
import br.com.gamemods.minecity.api.shape.PrecisePoint;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.api.world.EntityPos;
import br.com.gamemods.minecity.reactive.game.block.*;
import br.com.gamemods.minecity.reactive.game.item.ReactiveItemStack;
import br.com.gamemods.minecity.reactive.game.server.data.ChunkData;
import br.com.gamemods.minecity.reactive.game.server.data.ServerManipulator;
import br.com.gamemods.minecity.reactive.reaction.InteractReaction;
import br.com.gamemods.minecity.reactive.reaction.NoReaction;
import br.com.gamemods.minecity.reactive.reaction.Reaction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;

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

    boolean sendBlockUpdate(int x, int y, int z);

    default boolean sendBlockUpdate(Point point)
    {
        return sendBlockUpdate(point.x, point.y, point.z);
    }

    default InteractReaction onRightClick(Hand hand, ReactiveItemStack stack, ReactiveBlock block,
                                          @Nullable Direction side, PrecisePoint point)
    {
        Interaction event = new Interaction(Interaction.Click.RIGHT, this, hand, stack, block, side, point);

        event.getReactionItemFirst().combineAction(stack.rightClickFirst(event));
        event.getReactionBlock().combineBlock(block.rightClick(event));
        event.getReactionItemLast().combineItem(stack.rightClickFirst(event));

        return event.result();
    }

    default Reaction onBlockPlace(Modification modification)
    {
        return onBlockChange(modification, ReactiveItemStack::blockPlace);
    }

    default Reaction onBlockBreak(Modification modification)
    {
        return onBlockChange(modification, ReactiveItemStack::blockBreak);
    }

    default Reaction onBlockReplace(Modification modification)
    {
        return onBlockChange(modification, ReactiveItemStack::blockReplace);
    }

    default Reaction onBlockGrow(Modification modification)
    {
        return onBlockChange(modification, ReactiveItemStack::blockGrow);
    }

    default Reaction onBlockChange(Modification modification, BiFunction<ReactiveItemStack, Modification, Reaction> stackOperation)
    {
        AtomicReference<Reaction> reaction = new AtomicReference<>(
                modification.getUsedStack().map(stack-> stackOperation.apply(stack, modification)).orElse(NoReaction.INSTANCE)
        );

        modification.forEach(mod-> {
            BlockChange change = mod.getBlockChange();
            reaction.getAndUpdate(it-> it.combine(
                change.getOriginal().beingReplaced(mod).combine(
                        change.getReplaced().replacing(mod)
                )
            ));
        });

        return reaction.get();
    }

    default Reaction onBlockChangePre(PreModification modification)
    {
        AtomicReference<Reaction> reaction = new AtomicReference<>(
                modification.getUsedStack().map(stack-> stack.blockChangePre(modification)).orElse(NoReaction.INSTANCE)
        );

        modification.forEach(mod-> reaction.getAndUpdate(it-> it.combine(mod.getSnapshot().preModification(mod))));

        return reaction.get();
    }
}
