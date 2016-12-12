package br.com.gamemods.minecity.reactive.game.block.supplier;

import br.com.gamemods.minecity.reactive.game.entity.ReactiveEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

@FunctionalInterface
public interface SupplierReactiveEntity
{
    @Nullable
    Optional<? extends ReactiveEntity> getReactiveEntity();
}
