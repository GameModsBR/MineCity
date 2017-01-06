package br.com.gamemods.minecity.reactive.game.item.data;

import br.com.gamemods.minecity.reactive.ReactiveLayer;
import br.com.gamemods.minecity.reactive.game.item.ReactiveItemTrait;
import br.com.gamemods.minecity.reactive.game.item.data.supplier.SupplierItemTraitData;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

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

    default Optional<ReactiveItemTrait<T>> getReactiveItemTrait()
    {
        return ReactiveLayer.getReactor().getItemReactor().getReactiveItemTrait(this);
    }
}
