package br.com.gamemods.minecity.reactive.game.item.data;

import br.com.gamemods.minecity.reactive.game.item.ReactiveItemState;
import br.com.gamemods.minecity.reactive.game.item.ReactiveItemTrait;
import br.com.gamemods.minecity.reactive.game.item.data.supplier.SupplierItemStateData;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.stream.Stream;

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

    Optional<ReactiveItemState> getReactiveItemState();

    Stream<ItemTraitData<?>> itemTraitStream();

    default Stream<ReactiveItemTrait<?>> reactiveItemTraitStream()
    {
        return itemTraitStream().map(ItemTraitData::getReactiveItemTrait)
                .filter(Optional::isPresent).map(Optional::get);
    }
}
