package br.com.gamemods.minecity.reactive.vanilla.block;

import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.reactive.game.block.InteractEvent;
import br.com.gamemods.minecity.reactive.game.block.ReactiveBlockType;
import br.com.gamemods.minecity.reactive.game.block.data.BlockRole;
import br.com.gamemods.minecity.reactive.reaction.Reaction;
import br.com.gamemods.minecity.reactive.reaction.SingleBlockReaction;
import org.jetbrains.annotations.NotNull;

/**
 * A block that can store things inside it and open its storage on right click.
 */
public interface ReactiveBlockTypeContainer extends ReactiveBlockType
{
    ReactiveBlockTypeContainer INSTANCE = new ReactiveBlockTypeContainer(){};

    @NotNull
    @Override
    default BlockRole getBlockRole()
    {
        return BlockRole.CONTAINER;
    }

    @Override
    default Reaction reactRightClick(InteractEvent event)
    {
        return new SingleBlockReaction(event.getBlock().getPosition(), PermissionFlag.OPEN);
    }
}
