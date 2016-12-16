package br.com.gamemods.minecity.reactive.reactor;

import br.com.gamemods.minecity.reactive.game.item.ReactiveItem;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface ItemReactor
{
    @NotNull
    Optional<ReactiveItem> getReactiveItem(Object item);
}
