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
 * A block that structurally changes on right clicks. Like repeaters, you can break a redstone circuit when you
 * right click it.
 */
public interface ReactiveBlockModifiable<T extends Comparable<T>> extends ReactiveBlockType, ReactiveBlockTrait<T>
{
    ReactiveBlockModifiable<?> INSTANCE = new ReactiveBlockModifiable(){};

    @NotNull
    @Override
    default BlockRole getBlockRole()
    {
        return BlockRole.MODIFIABLE;
    }

    @Override
    default Reaction reactRightClick(Interaction event)
    {
        return new SingleBlockReaction(event.getBlock().getPosition(), PermissionFlag.MODIFY);
    }
}
