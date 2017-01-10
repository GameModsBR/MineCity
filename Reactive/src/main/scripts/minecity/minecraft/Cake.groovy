package minecity.minecraft

import br.com.gamemods.minecity.api.permission.PermissionFlag
import br.com.gamemods.minecity.reactive.game.block.*
import br.com.gamemods.minecity.reactive.game.block.data.BlockRole
import br.com.gamemods.minecity.reactive.reaction.*

class Cake implements ReactiveBlockType {

    BlockRole blockRole = BlockRole.CLICKABLE

    @Override
    Reaction reactRightClick(Interaction event) {
        new SingleBlockReaction(event.block.position, PermissionFlag.CLICK).onDenySendHunger()
    }

    @Override
    Reaction reactBeingBroken(Modification event) {
        if(event.entityCause.isPresent() && event.blockChange.original.blockStateData.getTrait('bites').map{it == 6}.orElse(false))
            new SingleBlockReaction(event.position, PermissionFlag.CLICK).onDenySendHunger()
        else
            new SingleBlockReaction(event.position, PermissionFlag.MODIFY)
    }
}
