package br.com.gamemods.minecity.reactive.game.block.data;

import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.reactive.ReactiveLayer;
import br.com.gamemods.minecity.reactive.game.block.BlockChange;
import br.com.gamemods.minecity.reactive.game.block.Modification;
import br.com.gamemods.minecity.reactive.game.block.PreModification;
import br.com.gamemods.minecity.reactive.game.block.ReactiveBlockProperty;
import br.com.gamemods.minecity.reactive.game.block.data.supplier.SupplierBlockSnapshotData;
import br.com.gamemods.minecity.reactive.game.item.ReactiveItemStack;
import br.com.gamemods.minecity.reactive.game.server.data.ChunkData;
import br.com.gamemods.minecity.reactive.reaction.NoReaction;
import br.com.gamemods.minecity.reactive.reaction.Reaction;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface BlockSnapshotData extends SupplierBlockSnapshotData
{
    Object getBlockSnapshot();

    BlockPos getPosition();

    Optional<TileEntityData> getTileEntityData();

    /**
     * Gets the extended BlockState. This applies extended properties
     * that are usually located around the associated BlockType.
     */
    Optional<BlockStateData> getExtendedBlockStateData();

    /**
     * Gets the {@link UUID}, if available, of the user who created this BlockSnapshot
     */
    Optional<UUID> getCreatorUUID();

    /**
     * Gets the {@link UUID}, if available, of the user who last notified this BlockSnapshot
     */
    Optional<UUID> getNotifierUUID();

    @NotNull
    @Override
    BlockStateData getBlockStateData();

    @NotNull
    @Override
    default BlockSnapshotData getBlockSnapshotData()
    {
        return this;
    }

    default Reaction beingReplaced(Modification mod)
    {
        BlockChange blockChange = mod.getBlockChange();
        BlockSnapshotData replaced = blockChange.getReplaced();
        Function<ReactiveBlockProperty, Reaction> method;

        if(replaced.getBlockTypeData().matches("minecraft:air"))
            method = it-> it.reactBeingBroken(mod);

        else if(replaced.getBlockTypeData().equals(blockChange.getOriginal().getBlockTypeData()))
            method = it-> it.reactStateChange(mod);

        else
            method = it-> it.reactBeingReplaced(mod);

        return ReactiveLayer.getBlockReactor().getReactiveBlockType(getBlockTypeData()).map(method).orElse(NoReaction.INSTANCE);
    }

    default Reaction replacing(Modification mod)
    {
        return ReactiveLayer.getBlockReactor().getReactiveBlockType(getBlockTypeData()).map(it-> it.reactPlaceOne(mod)).orElse(NoReaction.INSTANCE);
    }

    default Reaction preModification(PreModification mod)
    {
        return NoReaction.INSTANCE;
    }

    default Optional<ChunkData> getChunk()
    {
        return ReactiveLayer.getChunk(getPosition().getChunk());
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
