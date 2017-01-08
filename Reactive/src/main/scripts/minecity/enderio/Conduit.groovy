package minecity.enderio

import br.com.gamemods.minecity.api.permission.PermissionFlag
import br.com.gamemods.minecity.reactive.game.block.*
import br.com.gamemods.minecity.reactive.game.block.data.BlockRole
import br.com.gamemods.minecity.reactive.game.item.ReactiveItem
import br.com.gamemods.minecity.reactive.reaction.*

class Conduit implements ReactiveItem, ReactiveBlockType {

    BlockRole blockRole = BlockRole.MODIFIABLE

    @Override
    Reaction reactRightClickFirst(Interaction event) {
        if(event.hasBlock())
            new BlockAndSidesReaction(PermissionFlag.MODIFY, event.block.position)
        else
            NoReaction.INSTANCE
    }

    @Override
    Reaction reactRightClick(Interaction event) {
        reactRightClickFirst(event)
    }

    @Override
    Reaction reactLeftClick(Interaction event) {
        reactRightClickFirst(event)
    }
}
