package br.com.gamemods.minecity.reactive.game.block.data.supplier;

import br.com.gamemods.minecity.reactive.game.block.data.BlockTraitData;
import org.jetbrains.annotations.NotNull;

/**
 * An object that can provide information about a block trait.
 * @param <T> The trait value type
 */
@FunctionalInterface
public interface SupplierBlockTraitData<T extends Comparable<T>>
{
    /**
     * The block trait relative to this object
     */
    @NotNull
    BlockTraitData<T> getBlockTraitData();
}
