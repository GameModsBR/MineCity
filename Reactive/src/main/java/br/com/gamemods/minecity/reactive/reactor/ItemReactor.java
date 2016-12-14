package br.com.gamemods.minecity.reactive.reactor;

import br.com.gamemods.minecity.reactive.game.item.ReactiveItem;
import br.com.gamemods.minecity.reactive.game.item.data.ItemStackData;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface ItemReactor
{
    @NotNull
    Optional<ItemStackData> getStack(Object itemStack);

    @NotNull
    Optional<ReactiveItem> getItem(Object item);
}
