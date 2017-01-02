package br.com.gamemods.minecity.reactive.game.block;

import br.com.gamemods.minecity.reactive.game.block.data.BlockRole;
import org.jetbrains.annotations.NotNull;

/**
 * An object attached to the block type that reacts to block events
 */
public interface ReactiveBlockType extends ReactiveBlockProperty
{
    ReactiveBlockType DECORATIVE = new ReactiveBlockType(){};

    /**
     * The default role that this block represents.
     * @see ReactiveBlockState#getBlockRole()
     */
    @NotNull
    default BlockRole getBlockRole()
    {
        return BlockRole.DECORATIVE;
    }
}
