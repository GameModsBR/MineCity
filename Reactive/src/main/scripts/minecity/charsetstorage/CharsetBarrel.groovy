package minecity.charsetstorage

import br.com.gamemods.minecity.api.permission.PermissionFlag
import br.com.gamemods.minecity.reactive.game.block.*
import br.com.gamemods.minecity.reactive.game.block.data.BlockRole
import br.com.gamemods.minecity.reactive.reaction.*

class CharsetBarrel implements ReactiveBlockType {

    BlockRole blockRole = BlockRole.CONTAINER

    @Override
    Reaction reactRightClick(Interaction event) {
        new SingleBlockReaction(event.block.position, PermissionFlag.OPEN)
    }

    @Override
    Reaction reactLeftClick(Interaction event) {
        new SingleBlockReaction(event.block.position, PermissionFlag.OPEN)
    }
}
