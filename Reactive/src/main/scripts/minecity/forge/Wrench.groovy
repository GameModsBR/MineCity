package minecity.forge

import br.com.gamemods.minecity.api.permission.PermissionFlag
import br.com.gamemods.minecity.reactive.game.block.Interaction
import br.com.gamemods.minecity.reactive.game.item.ReactiveItem
import br.com.gamemods.minecity.reactive.reaction.*

class Wrench implements ReactiveItem {
    @Override
    Reaction reactRightClickFirst(Interaction event) {
        if(event.hasBlock())
            new SingleBlockReaction(event.block.position, PermissionFlag.MODIFY)
        else
            NoReaction.INSTANCE
    }

    @Override
    Reaction reactLeftClickFirst(Interaction event) {
        if(event.hasBlock())
            new SingleBlockReaction(event.block.position, PermissionFlag.MODIFY)
        else
            NoReaction.INSTANCE
    }
}
