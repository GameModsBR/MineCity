package br.com.gamemods.minecity.reactive.game.item.data;

import br.com.gamemods.minecity.reactive.game.item.data.supplier.SupplierItemTraitData;
import org.jetbrains.annotations.NotNull;

/**
 * Extended item property that is usually stored in NBT tags in item stacks
 */
public interface ItemTraitData<T> extends SupplierItemTraitData<T>
{
    @NotNull
    @Override
    default ItemTraitData<T> getItemTraitData()
    {
        return this;
    }
}
