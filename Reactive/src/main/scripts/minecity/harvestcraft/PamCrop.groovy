package minecity.harvestcraft

import br.com.gamemods.minecity.api.permission.PermissionFlag
import br.com.gamemods.minecity.reactive.game.block.*
import br.com.gamemods.minecity.reactive.game.block.data.BlockRole
import br.com.gamemods.minecity.reactive.reaction.*

class PamCrop implements ReactiveBlockType {

    BlockRole blockRole = BlockRole.HARVESTEABLE

    @Override
    Reaction reactRightClick(Interaction event) {
        new SingleBlockReaction(event.block.position, PermissionFlag.HARVEST)
    }
}
