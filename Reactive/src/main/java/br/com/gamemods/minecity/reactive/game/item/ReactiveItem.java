package br.com.gamemods.minecity.reactive.game.item;

import br.com.gamemods.minecity.reactive.game.block.ReactiveBlockState;

import java.util.Optional;

public interface ReactiveItem
{
    default Optional<ReactiveBlockState> getReactiveBlockState()
    {
        return Optional.empty();
    }
}
