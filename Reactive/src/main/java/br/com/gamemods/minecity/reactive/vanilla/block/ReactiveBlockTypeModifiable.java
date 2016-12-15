package br.com.gamemods.minecity.reactive.vanilla.block;

import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.reactive.game.block.InteractEvent;
import br.com.gamemods.minecity.reactive.game.block.ReactiveBlockType;
import br.com.gamemods.minecity.reactive.game.block.data.BlockRole;
import br.com.gamemods.minecity.reactive.reaction.Reaction;
import br.com.gamemods.minecity.reactive.reaction.SingleBlockReaction;
import org.jetbrains.annotations.NotNull;

/**
 * A block that structurally changes on right clicks. Like repeaters, you can break a redstone circuit when you
 * right click it.
 */
public interface ReactiveBlockTypeModifiable extends ReactiveBlockType
{
    ReactiveBlockTypeModifiable INSTANCE = new ReactiveBlockTypeModifiable(){};

    @NotNull
    @Override
    default BlockRole getBlockRole()
    {
        return BlockRole.MODIFIABLE;
    }

    @Override
    default Reaction reactRightClick(InteractEvent event)
    {
        return new SingleBlockReaction(event.getBlock().getPosition(), PermissionFlag.MODIFY);
    }
}
