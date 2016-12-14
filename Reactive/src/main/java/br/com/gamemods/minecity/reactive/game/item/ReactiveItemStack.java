package br.com.gamemods.minecity.reactive.game.item;

import br.com.gamemods.minecity.reactive.game.item.data.ItemStackData;
import br.com.gamemods.minecity.reactive.game.item.data.supplier.SupplierItemStackData;
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
}
