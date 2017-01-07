package br.com.gamemods.minecity.reactive.game.item;

import br.com.gamemods.minecity.reactive.game.block.Interaction;
import br.com.gamemods.minecity.reactive.game.block.Modification;
import br.com.gamemods.minecity.reactive.game.block.PreModification;
import br.com.gamemods.minecity.reactive.game.item.data.ItemStackData;
import br.com.gamemods.minecity.reactive.game.item.data.supplier.SupplierItemStackData;
import br.com.gamemods.minecity.reactive.reaction.NoReaction;
import br.com.gamemods.minecity.reactive.reaction.Reaction;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.stream.Stream;

public final class ReactiveItemStack implements SupplierItemStackData
{
    @NotNull
    private final ItemStackData stack;

    public ReactiveItemStack(@NotNull ItemStackData stack)
    {
        this.stack = stack;
    }

    @NotNull
    public Stream<ReactiveItemProperty> propertyStream()
    {
        return Stream.concat(
                Stream.of(
                        stack.getItemData().getReactiveItemType().orElse(null),
                        stack.getItemStateData().getReactiveItemState().orElse(null)
                ),
                stack.getItemStateData().reactiveItemTraitStream()
        ).filter(Objects::nonNull);
    }

    @NotNull
    @Override
    public ItemStackData getItemStackData()
    {
        return stack;
    }

    @NotNull
    public Reaction rightClickFirst(Interaction event)
    {
        if(event.getStack() != this)  throw new IllegalArgumentException(event.getStack()+" != "+this);

        return propertyStream()
                .map(prop -> prop.reactRightClickFirst(event))
                .reduce(Reaction::combine)
                .orElse(NoReaction.INSTANCE);
    }

    @NotNull
    public Reaction rightClickLast(Interaction event)
    {
        if(event.getStack() != this) throw new IllegalArgumentException(event.getStack()+" != "+this);

        return propertyStream()
                .map(prop -> prop.reactRightClickLast(event))
                .reduce(Reaction::combine)
                .orElse(NoReaction.INSTANCE);
    }

    @NotNull
    public Reaction leftClickFirst(Interaction event)
    {
        if(event.getStack() != this)  throw new IllegalArgumentException(event.getStack()+" != "+this);

        return propertyStream()
                .map(prop -> prop.reactLeftClickFirst(event))
                .reduce(Reaction::combine)
                .orElse(NoReaction.INSTANCE);
    }

    @NotNull
    public Reaction leftClickLast(Interaction event)
    {
        if(event.getStack() != this) throw new IllegalArgumentException(event.getStack()+" != "+this);

        return propertyStream()
                .map(prop -> prop.reactLeftClickLast(event))
                .reduce(Reaction::combine)
                .orElse(NoReaction.INSTANCE);
    }

    public Reaction blockPlace(Modification modification)
    {
        return NoReaction.INSTANCE;
    }

    public Reaction blockBreak(Modification modification)
    {
        return NoReaction.INSTANCE;
    }

    public Reaction blockReplace(Modification modification)
    {
        return NoReaction.INSTANCE;
    }

    public Reaction blockGrow(Modification modification)
    {
        return NoReaction.INSTANCE;
    }

    public Reaction blockChangePre(PreModification modification)
    {
        return NoReaction.INSTANCE;
    }
}
