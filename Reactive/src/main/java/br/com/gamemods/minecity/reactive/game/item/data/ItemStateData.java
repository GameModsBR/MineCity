package br.com.gamemods.minecity.reactive.game.item.data;

import br.com.gamemods.minecity.reactive.game.item.data.supplier.SupplierItemStateData;
import org.jetbrains.annotations.NotNull;

/**
 * An item property that is usually represented as an integer in item stacks.
 */
public interface ItemStateData extends SupplierItemStateData
{
    @NotNull
    @Override
    default ItemStateData getItemStateData()
    {
        return this;
    }
}
