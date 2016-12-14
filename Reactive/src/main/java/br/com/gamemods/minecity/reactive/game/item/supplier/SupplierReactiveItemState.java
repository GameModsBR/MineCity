package br.com.gamemods.minecity.reactive.game.item.supplier;

import br.com.gamemods.minecity.reactive.game.item.ReactiveItemState;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface SupplierReactiveItemState
{
    @NotNull
    Optional<ReactiveItemState> getReactiveItemState();
}
