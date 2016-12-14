package br.com.gamemods.minecity.reactive.game.item.data.supplier;

import br.com.gamemods.minecity.reactive.game.item.data.ItemTraitData;
import org.jetbrains.annotations.NotNull;

public interface SupplierItemTraitData<T>
{
    @NotNull
    ItemTraitData<T> getItemTraitData();
}
