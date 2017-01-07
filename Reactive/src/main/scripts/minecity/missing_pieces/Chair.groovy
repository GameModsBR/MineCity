package minecity.missing_pieces

import br.com.gamemods.minecity.api.permission.PermissionFlag
import br.com.gamemods.minecity.reactive.game.block.*
import br.com.gamemods.minecity.reactive.reaction.*

class Chair implements ReactiveBlockType {

    @Override
    Reaction reactRightClick(Interaction event) {
        return new SingleBlockReaction(event.block.position, PermissionFlag.ENTER)
    }
}
