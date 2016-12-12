package br.com.gamemods.minecity.reactive.game.block;

import br.com.gamemods.minecity.reactive.game.block.data.BlockRole;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * An object attached to the block states that reacts to block events
 */
public interface ReactiveBlockState extends ReactiveBlockProperty
{
    /**
     * The role which this reactive state represents.
     * @return An empty object if this state does not overrides the default
     */
    @NotNull
    default Optional<BlockRole> getBlockRole()
    {
        return Optional.empty();
    }
}
