package br.com.gamemods.minecity.reactive.game.entity.data;

import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.permission.Identity;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.shape.Point;
import br.com.gamemods.minecity.api.shape.PrecisePoint;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.api.world.EntityPos;
import br.com.gamemods.minecity.reactive.game.block.*;
import br.com.gamemods.minecity.reactive.game.entity.data.supplier.SupplierEntityData;
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

public interface EntityData extends SupplierEntityData
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

    Identity<?> getIdentity();

    boolean isSneaking();

    boolean sendInventoryUpdate();

    boolean sendBlockUpdate(int x, int y, int z);

    default boolean sendBlockUpdate(Point point)
    {
        return sendBlockUpdate(point.x, point.y, point.z);
    }

    boolean sendHungerUpdate();

    Optional<Message> can(PermissionFlag perm, BlockPos pos);

    default InteractReaction onLeftClick(Hand hand, ReactiveItemStack stack, @Nullable ReactiveBlock block,
                                          @Nullable Direction side, PrecisePoint point)
    {
        Interaction event = new Interaction(Interaction.Click.LEFT, this, hand, stack, block, side, point);

        event.getReactionItemFirst().combineAction(stack.leftClickFirst(event));
        if(block != null)
            event.getReactionBlock().combineBlock(block.leftClick(event));
        event.getReactionItemLast().combineItem(stack.leftClickLast(event));

        return event.result();
    }

    default InteractReaction onRightClick(Hand hand, ReactiveItemStack stack, @Nullable ReactiveBlock block,
                                          @Nullable Direction side, PrecisePoint point)
    {
        Interaction event = new Interaction(Interaction.Click.RIGHT, this, hand, stack, block, side, point);

        event.getReactionItemFirst().combineAction(stack.rightClickFirst(event));
        if(block != null)
            event.getReactionBlock().combineBlock(block.rightClick(event));
        event.getReactionItemLast().combineItem(stack.rightClickLast(event));

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

    void send(Message message);
    void send(Message[] messages);

    @NotNull
    @Override
    default EntityData getEntityData()
    {
        return this;
    }
}
