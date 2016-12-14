package br.com.gamemods.minecity.reactive.game.block.supplier;

import br.com.gamemods.minecity.reactive.game.block.ReactiveBlockState;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

@FunctionalInterface
public interface SupplierReactiveBlockState
{
    @NotNull
    Optional<ReactiveBlockState> getReactiveBlockState();
}
