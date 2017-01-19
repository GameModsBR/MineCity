package br.com.gamemods.minecity.reactive.game.block;

import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.reactive.game.block.data.BlockRole;
import br.com.gamemods.minecity.reactive.reaction.Reaction;
import br.com.gamemods.minecity.reactive.reaction.SingleBlockReaction;
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

    @Override
    default Reaction reactPreModification(PreModification event)
    {
        switch(getBlockRole())
        {
            case HARVESTEABLE:
                return new SingleBlockReaction(event.getSnapshot().getPosition(), PermissionFlag.HARVEST);

            default:
                return new SingleBlockReaction(event.getSnapshot().getPosition(), PermissionFlag.MODIFY);
        }
    }

    @Override
    default Reaction reactBeingBroken(Modification event)
    {
        return new SingleBlockReaction(event.getPosition(), PermissionFlag.MODIFY);
    }

    @Override
    default Reaction reactBeingReplaced(Modification event)
    {
        return new SingleBlockReaction(event.getPosition(), PermissionFlag.MODIFY);
    }
}
