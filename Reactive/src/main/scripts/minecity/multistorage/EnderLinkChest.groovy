package minecity.multistorage

import br.com.gamemods.minecity.api.permission.PermissionFlag
import br.com.gamemods.minecity.reactive.game.block.*
import br.com.gamemods.minecity.reactive.game.block.data.BlockRole
import br.com.gamemods.minecity.reactive.reaction.*

class EnderLinkChest implements ReactiveBlockType {

    BlockRole blockRole = BlockRole.CONTAINER

    @Override
    Reaction reactLeftClick(Interaction event) {
        new SingleBlockReaction(event.block.position, PermissionFlag.MODIFY)
    }

    @Override
    Reaction reactRightClick(Interaction event) {
        if(event.entity.sneaking)
            new SingleBlockReaction(event.block.position, PermissionFlag.MODIFY)
        else
            new SingleBlockReaction(event.block.position, PermissionFlag.OPEN)
    }
}
