package minecity.supercraftingframe

import br.com.gamemods.minecity.api.permission.PermissionFlag
import br.com.gamemods.minecity.reactive.game.block.*
import br.com.gamemods.minecity.reactive.game.block.data.BlockRole
import br.com.gamemods.minecity.reactive.reaction.*

class CraftingFrame implements ReactiveBlockType {

    BlockRole blockRole = BlockRole.CLICKABLE

    @Override
    Reaction reactRightClick(Interaction event) {
        event.reactionBlock.combineAction new SingleBlockReaction(event.block.position, PermissionFlag.OPEN)
        NoReaction.INSTANCE
    }

    @Override
    Reaction reactLeftClick(Interaction event) {
        event.reactionBlock.combineAction new SingleBlockReaction(event.block.position, PermissionFlag.OPEN)
        NoReaction.INSTANCE
    }
}
