package br.com.gamemods.minecity.reactive.reactor;

import br.com.gamemods.minecity.reactive.game.item.ReactiveItem;
import br.com.gamemods.minecity.reactive.game.item.ReactiveItemState;
import br.com.gamemods.minecity.reactive.game.item.ReactiveItemTrait;
import br.com.gamemods.minecity.reactive.game.item.data.ItemStateData;
import br.com.gamemods.minecity.reactive.game.item.data.ItemTraitData;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface ItemReactor
{
    @NotNull
    Optional<ReactiveItem> getReactiveItem(Object item);

    @NotNull
    Optional<ReactiveItemState> getReactiveItemState(ItemStateData ItemState);

    @NotNull
    <T> Optional<ReactiveItemTrait<T>> getReactiveItemTrait(ItemTraitData<T> itemTrait);
}
