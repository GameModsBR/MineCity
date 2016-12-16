package br.com.gamemods.minecity.reactive.game.block.data;

import br.com.gamemods.minecity.reactive.ReactiveLayer;
import br.com.gamemods.minecity.reactive.game.block.ReactiveBlockTrait;
import br.com.gamemods.minecity.reactive.game.block.data.supplier.SupplierBlockTraitData;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * The information about a trait that a block state may have.
 * @param <T> The value type
 */
public interface BlockTraitData<T extends Comparable<T>> extends SupplierBlockTraitData<T>
{
    Object getBlockTrait();

    /**
     * The reactive object that will react to events related to this trait.
     */
    @NotNull
    default Optional<ReactiveBlockTrait<T>> getReactiveBlockTrait()
    {
        return ReactiveLayer.getReactor().getBlockReactor().getReactiveBlockTrait(this);
    }

    /**
     * The name of this trait, it can be linked to the Minecraft block trait system.
     */
    @NotNull
    String getName();

    /**
     * An unordered immutable collection with all possible values
     */
    @NotNull
    Collection<T> getPossibleValues();

    /**
     * The type of value that this trait stores.
     */
    @NotNull
    Class<T> getValueClass();

    /**
     * The {@link Predicate} used to validate if a given value is a valid value for this trait.
     */
    @NotNull
    Predicate<T> getValidator();

    /**
     * Returns itself
     */
    @NotNull
    @Override
    default BlockTraitData<T> getBlockTraitData()
    {
        return this;
    }
}
