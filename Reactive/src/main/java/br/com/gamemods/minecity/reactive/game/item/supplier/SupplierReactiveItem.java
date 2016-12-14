package br.com.gamemods.minecity.reactive.game.item.supplier;

import br.com.gamemods.minecity.reactive.game.item.ReactiveItem;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface SupplierReactiveItem
{
    @NotNull
    Optional<ReactiveItem> getReactiveItem();
}
