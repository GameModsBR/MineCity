package minecity.minecraft

import br.com.gamemods.minecity.api.permission.PermissionFlag
import br.com.gamemods.minecity.reactive.game.block.*
import br.com.gamemods.minecity.reactive.game.block.data.BlockRole
import br.com.gamemods.minecity.reactive.reaction.*

class Bed implements ReactiveBlockType {

    @Override
    BlockRole getBlockRole() {
        BlockRole.CLICKABLE
    }

    @Override
    Reaction reactRightClick(Interaction event) {

        new SingleBlockReaction(event.block.position, PermissionFlag.CLICK).combine(
                new SingleBlockReaction(event.block.position, PermissionFlag.ENTER)
        )

    }
}
