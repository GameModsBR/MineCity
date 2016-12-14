package br.com.gamemods.minecity.reactive.game.block.supplier;

import br.com.gamemods.minecity.reactive.game.block.ReactiveBlockTrait;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

@FunctionalInterface
public interface SupplierReactiveBlockTrait<T extends Comparable<T>>
{
    @NotNull
    Optional<ReactiveBlockTrait<T>> getReactiveBlockTrait();
}
