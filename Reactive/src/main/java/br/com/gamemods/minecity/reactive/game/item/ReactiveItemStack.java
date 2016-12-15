package br.com.gamemods.minecity.reactive.game.item;

import br.com.gamemods.minecity.reactive.game.block.Interaction;
import br.com.gamemods.minecity.reactive.game.block.Modification;
import br.com.gamemods.minecity.reactive.game.block.PreModification;
import br.com.gamemods.minecity.reactive.game.item.data.ItemStackData;
import br.com.gamemods.minecity.reactive.game.item.data.supplier.SupplierItemStackData;
import br.com.gamemods.minecity.reactive.reaction.NoReaction;
import br.com.gamemods.minecity.reactive.reaction.Reaction;
import org.jetbrains.annotations.NotNull;

public final class ReactiveItemStack implements SupplierItemStackData
{
    @NotNull
    private final ItemStackData stack;

    public ReactiveItemStack(@NotNull ItemStackData stack)
    {
        this.stack = stack;
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
        return NoReaction.INSTANCE;
    }

    @NotNull
    public Reaction rightClickLast(Interaction event)
    {
        return NoReaction.INSTANCE;
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
