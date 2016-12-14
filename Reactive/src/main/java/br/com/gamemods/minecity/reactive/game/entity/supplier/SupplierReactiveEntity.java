package br.com.gamemods.minecity.reactive.game.entity.supplier;

import br.com.gamemods.minecity.reactive.game.entity.ReactiveEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

@FunctionalInterface
public interface SupplierReactiveEntity
{
    @NotNull
    Optional<ReactiveEntity> getReactiveEntity();
}
