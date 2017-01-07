package br.com.gamemods.minecity.reactive.game.item;

import br.com.gamemods.minecity.reactive.game.block.Interaction;
import br.com.gamemods.minecity.reactive.reaction.NoReaction;
import br.com.gamemods.minecity.reactive.reaction.Reaction;

public interface ReactiveItemProperty
{
    default Reaction reactRightClickFirst(Interaction event)
    {
        return NoReaction.INSTANCE;
    }

    default Reaction reactRightClickLast(Interaction event)
    {
        return NoReaction.INSTANCE;
    }

    default Reaction reactLeftClickFirst(Interaction event)
    {
        return NoReaction.INSTANCE;
    }

    default Reaction reactLeftClickLast(Interaction event)
    {
        return NoReaction.INSTANCE;
    }
}
