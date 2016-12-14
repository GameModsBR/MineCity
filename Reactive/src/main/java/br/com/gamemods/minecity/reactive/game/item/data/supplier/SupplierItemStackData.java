package br.com.gamemods.minecity.reactive.game.item.data.supplier;

import br.com.gamemods.minecity.reactive.game.item.data.ItemData;
import br.com.gamemods.minecity.reactive.game.item.data.ItemStackData;
import br.com.gamemods.minecity.reactive.game.item.data.ItemStateData;
import org.jetbrains.annotations.NotNull;

public interface SupplierItemStackData extends SupplierItemStateData, SupplierItemData
{
    @NotNull
    ItemStackData getItemStackData();

    @NotNull
    @Override
    default ItemData getItemData()
    {
        return getItemStackData().getItemData();
    }

    @NotNull
    @Override
    default ItemStateData getItemStateData()
    {
        return getItemStackData().getItemStateData();
    }
}
