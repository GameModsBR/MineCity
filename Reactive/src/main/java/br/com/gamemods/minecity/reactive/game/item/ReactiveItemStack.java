package br.com.gamemods.minecity.reactive.game.item;

import br.com.gamemods.minecity.reactive.game.block.InteractEvent;
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
    public Reaction rightClickFirst(InteractEvent event)
    {
        return NoReaction.INSTANCE;
    }

    @NotNull
    public Reaction rightClickLast(InteractEvent event)
    {
        return NoReaction.INSTANCE;
    }
}
