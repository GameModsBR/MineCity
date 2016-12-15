package br.com.gamemods.minecity.reactive.game.block;

import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.reactive.ReactiveLayer;
import br.com.gamemods.minecity.reactive.game.entity.data.EntityData;
import br.com.gamemods.minecity.reactive.game.entity.data.Hand;
import br.com.gamemods.minecity.reactive.game.item.ReactiveItemStack;
import br.com.gamemods.minecity.reactive.game.server.data.ChunkData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public final class Modification
{
    @NotNull
    private final Object cause;

    @NotNull
    private final List<BlockChange> changes;

    @Nullable
    private final ReactiveItemStack usedStack;

    @Nullable
    private final Hand usedHand;

    private int cursor;

    public Modification(@NotNull List<BlockChange> changes, @NotNull Object cause,
                        @Nullable ReactiveItemStack usedStack, @Nullable Hand hand)
    {
        this.changes = Collections.unmodifiableList(new ArrayList<>(changes));
        this.cause = cause;
        this.usedStack = usedStack;
        this.usedHand = hand;
    }

    public void forEach(Consumer<Modification> action)
    {
        int originalPos = cursor;
        AtomicInteger pos = new AtomicInteger();
        changes.forEach(change-> {
            cursor = pos.getAndIncrement();
            action.accept(this);
        });
        cursor = originalPos;
    }

    public BlockChange getBlockChange()
    {
        return changes.get(cursor);
    }

    public boolean isValid()
    {
        return changes.get(cursor).isValid();
    }

    public void setValid(boolean valid)
    {
        changes.get(cursor).setValid(valid);
    }

    public BlockPos getPosition()
    {
        return changes.get(cursor).getOriginal().getPosition();
    }

    public int getCursor()
    {
        return cursor;
    }

    public void setCursor(int cursor)
    {
        this.cursor = cursor;
    }

    @NotNull
    public List<BlockChange> getChanges()
    {
        return changes;
    }

    @NotNull
    public Object getCause()
    {
        return cause;
    }

    @NotNull
    public Optional<EntityData> getEntityCause()
    {
        return ReactiveLayer.getEntityData(cause);
    }

    @NotNull
    public Optional<ReactiveItemStack> getUsedStack()
    {
        return Optional.ofNullable(usedStack);
    }

    @NotNull
    public Optional<Hand> getUsedHand()
    {
        return Optional.ofNullable(usedHand);
    }

    public ChunkData getChunk()
    {
        return getBlockChange().getOriginal().getChunk().get();
    }
}
