package minecity.storagedrawers

import br.com.gamemods.minecity.api.permission.PermissionFlag
import br.com.gamemods.minecity.reactive.game.block.*
import br.com.gamemods.minecity.reactive.game.block.data.BlockRole
import br.com.gamemods.minecity.reactive.reaction.*

class Drawer implements ReactiveBlockType {

    BlockRole blockRole = BlockRole.CONTAINER

    @Override
    Reaction reactLeftClick(Interaction event) {
        new SingleBlockReaction(event.block.position, PermissionFlag.OPEN)
            .onDenySendBlockUpdateAt(event.block.position)
            .onDenySendInventoryUpdate()
    }

    @Override
    Reaction reactRightClick(Interaction event) {
        new SingleBlockReaction(event.block.position, PermissionFlag.OPEN)
    }
}
