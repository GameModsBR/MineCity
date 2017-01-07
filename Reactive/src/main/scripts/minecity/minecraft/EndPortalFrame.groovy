package minecity.minecraft

import br.com.gamemods.minecity.api.permission.PermissionFlag
import br.com.gamemods.minecity.reactive.game.block.*
import br.com.gamemods.minecity.reactive.reaction.*

class EndPortalFrame implements ReactiveBlockType {

    @Override
    Reaction reactStateChange(Modification event) {
        new SingleBlockReaction(event.position, PermissionFlag.MODIFY)
    }

}
