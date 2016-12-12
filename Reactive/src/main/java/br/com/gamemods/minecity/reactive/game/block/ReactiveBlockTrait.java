package br.com.gamemods.minecity.reactive.game.block;

/**
 * An object attached to the block traits that reacts to block events
 * @param <T> The trait value type
 */
public interface ReactiveBlockTrait<T extends Comparable<T>> extends ReactiveBlockProperty
{
}
