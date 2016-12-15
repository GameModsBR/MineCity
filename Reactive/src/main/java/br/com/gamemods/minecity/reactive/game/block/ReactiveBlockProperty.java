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
    default Reaction reactRightClick(InteractEvent event)
    {
        return NoReaction.INSTANCE;
    }

    /**
     * Determine the reaction that would happen when an entity left clicks it
     */
    default Reaction reactLeftClick(InteractEvent event)
    {
        return NoReaction.INSTANCE;
    }
}
