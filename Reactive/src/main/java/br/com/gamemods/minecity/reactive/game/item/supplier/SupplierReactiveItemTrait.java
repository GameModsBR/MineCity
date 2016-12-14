package br.com.gamemods.minecity.reactive.game.item.supplier;

import br.com.gamemods.minecity.reactive.game.item.ReactiveItemTrait;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface SupplierReactiveItemTrait<T>
{
    @NotNull
    Optional<ReactiveItemTrait<T>> getReactiveItemTrait();
}
