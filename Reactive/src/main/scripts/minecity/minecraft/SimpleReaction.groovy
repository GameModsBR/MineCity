package minecity.minecraft

import br.com.gamemods.minecity.api.permission.PermissionFlag
import br.com.gamemods.minecity.reactive.game.block.*
import br.com.gamemods.minecity.reactive.game.block.data.BlockRole
import br.com.gamemods.minecity.reactive.reaction.*

class SimpleReaction implements ReactiveBlockType {

    BlockRole blockRole = BlockRole.DECORATIVE
    PermissionFlag rightClick, leftClick

    @Override
    Reaction reactRightClick(Interaction event) {
        if(rightClick)
            new SingleBlockReaction(event.block.position, rightClick)
        else
            NoReaction.INSTANCE
    }

    @Override
    Reaction reactLeftClick(Interaction event) {
        if(leftClick)
            new SingleBlockReaction(event.block.position, leftClick)
        else
            NoReaction.INSTANCE
    }
}
