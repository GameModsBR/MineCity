package minecity.cookingforblockheads

import br.com.gamemods.minecity.api.permission.PermissionFlag
import br.com.gamemods.minecity.reactive.game.block.*
import br.com.gamemods.minecity.reactive.game.block.data.BlockRole
import br.com.gamemods.minecity.reactive.reaction.*

class MilkJar implements ReactiveBlockType {

    BlockRole blockRole = BlockRole.CONTAINER

    @Override
    Reaction reactRightClick(Interaction event) {
        new SingleBlockReaction(event.block.position, PermissionFlag.OPEN)
            .onDenySendBlockUpdateAt(event.block.position)
            .onDenySendInventoryUpdate()
    }
}
