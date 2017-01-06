package br.com.gamemods.minecity.reactive.game.block;

import br.com.gamemods.minecity.reactive.reaction.NoReaction;
import br.com.gamemods.minecity.reactive.reaction.Reaction;

/**
 * An object that reacts to events related to a block
 */
public interface ReactiveBlockProperty
{
    /**
     * Determine the reaction that would happen when an entity right clicks it
     */
    default Reaction reactRightClick(Interaction event)
    {
        return NoReaction.INSTANCE;
    }

    /**
     * Determine the reaction that would happen when an entity left clicks it
     */
    default Reaction reactLeftClick(Interaction event)
    {
        return NoReaction.INSTANCE;
    }

    default Reaction reactPlaceOne(Modification event)
    {
        return NoReaction.INSTANCE;
    }

    default Reaction reactPlaceMany(Modification event)
    {
        return NoReaction.INSTANCE;
    }

    default Reaction reactBeingReplaced(Modification event)
    {
        return NoReaction.INSTANCE;
    }

    default Reaction reactBeingBroken(Modification event)
    {
        return NoReaction.INSTANCE;
    }
}
