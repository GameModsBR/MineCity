package br.com.gamemods.minecity.reactive.game.block.supplier;

import br.com.gamemods.minecity.reactive.game.block.ReactiveBlockType;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

@FunctionalInterface
public interface SupplierReactiveBlockType
{
    @NotNull
    Optional<ReactiveBlockType> getReactiveBlockType();
}
