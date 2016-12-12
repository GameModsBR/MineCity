package br.com.gamemods.minecity.reactive.game.block.supplier;

import br.com.gamemods.minecity.reactive.game.block.ReactiveTileEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

@FunctionalInterface
public interface SupplierReactiveTileEntity
{
    @NotNull
    Optional<? extends ReactiveTileEntity> getReactiveTileEntity();
}
