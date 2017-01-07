package minecity.mcmultipart

import br.com.gamemods.minecity.api.permission.PermissionFlag
import br.com.gamemods.minecity.reactive.game.block.*
import br.com.gamemods.minecity.reactive.game.block.data.BlockRole
import br.com.gamemods.minecity.reactive.reaction.*

class MultiPart implements ReactiveBlockType {

    BlockRole blockRole = BlockRole.MODIFIABLE

    @Override
    Reaction reactPreModification(PreModification mod) {
        new SingleBlockReaction(mod.snapshot.position, PermissionFlag.MODIFY)
    }
}
