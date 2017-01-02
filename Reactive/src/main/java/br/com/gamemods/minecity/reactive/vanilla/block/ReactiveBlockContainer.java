package br.com.gamemods.minecity.reactive.vanilla.block;

import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.reactive.game.block.Interaction;
import br.com.gamemods.minecity.reactive.game.block.ReactiveBlockTrait;
import br.com.gamemods.minecity.reactive.game.block.ReactiveBlockType;
import br.com.gamemods.minecity.reactive.game.block.data.BlockRole;
import br.com.gamemods.minecity.reactive.reaction.Reaction;
import br.com.gamemods.minecity.reactive.reaction.SingleBlockReaction;
import org.jetbrains.annotations.NotNull;

/**
 * A block that can store things inside it and open its storage on right click.
 */
public interface ReactiveBlockContainer<T extends Comparable<T>> extends ReactiveBlockType, ReactiveBlockTrait<T>
{
    ReactiveBlockContainer<?> INSTANCE = new ReactiveBlockContainer(){};

    @NotNull
    @Override
    default BlockRole getBlockRole()
    {
        return BlockRole.CONTAINER;
    }

    @Override
    default Reaction reactRightClick(Interaction event)
    {
        return new SingleBlockReaction(event.getBlock().getPosition(), PermissionFlag.OPEN);
    }
}
