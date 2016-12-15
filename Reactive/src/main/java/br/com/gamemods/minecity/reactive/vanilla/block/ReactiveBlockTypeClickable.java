package br.com.gamemods.minecity.reactive.vanilla.block;

import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.reactive.game.block.InteractEvent;
import br.com.gamemods.minecity.reactive.game.block.ReactiveBlockType;
import br.com.gamemods.minecity.reactive.game.block.data.BlockRole;
import br.com.gamemods.minecity.reactive.reaction.Reaction;
import br.com.gamemods.minecity.reactive.reaction.SingleBlockReaction;
import org.jetbrains.annotations.NotNull;

/**
 * A block that has simple click reactions, like wooden doors and buttons.
 */
public interface ReactiveBlockTypeClickable extends ReactiveBlockType
{
    ReactiveBlockTypeClickable INSTANCE = new ReactiveBlockTypeClickable(){};

    @NotNull
    @Override
    default BlockRole getBlockRole()
    {
        return BlockRole.CLICKABLE;
    }

    @Override
    default Reaction reactRightClick(InteractEvent event)
    {
        return new SingleBlockReaction(event.getBlock().getPosition(), PermissionFlag.CLICK);
    }
}
