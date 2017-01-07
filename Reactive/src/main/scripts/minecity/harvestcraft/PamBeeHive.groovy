package minecity.harvestcraft

import br.com.gamemods.minecity.api.permission.*
import br.com.gamemods.minecity.reactive.game.block.*
import br.com.gamemods.minecity.reactive.game.block.data.BlockRole
import br.com.gamemods.minecity.reactive.reaction.*

class PamBeeHive implements ReactiveBlockType {

    BlockRole blockRole = BlockRole.HARVESTEABLE

    @Override
    Reaction reactBeingBroken(Modification event) {
        if(event.chunk.getOwner(event.position).type == Identity.Type.NATURE)
            new SingleBlockReaction(event.position, PermissionFlag.HARVEST)
        else
            new SingleBlockReaction(event.position, PermissionFlag.MODIFY)
    }
}
