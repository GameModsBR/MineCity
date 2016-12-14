package br.com.gamemods.minecity.reactive.game.item.data;

import br.com.gamemods.minecity.reactive.game.item.data.supplier.SupplierItemStackData;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

public interface ItemStackData extends SupplierItemStackData
{
    @Override
    ItemData getItemData();

    @Override
    ItemStateData getItemStateData();

    Stream<ItemTraitData<?>> itemTraitStream();

    @NotNull
    @Override
    default ItemStackData getItemStackData()
    {
        return this;
    }
}
