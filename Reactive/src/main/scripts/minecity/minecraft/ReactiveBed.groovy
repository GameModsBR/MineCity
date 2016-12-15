package minecity.minecraft

import br.com.gamemods.minecity.api.permission.PermissionFlag
import br.com.gamemods.minecity.reactive.game.block.*
import br.com.gamemods.minecity.reactive.reaction.*

class ReactiveBed implements ReactiveBlockType {
    @Override
    Reaction reactRightClick(InteractEvent event) {

        return new SingleBlockReaction(event.block.position, PermissionFlag.CLICK).combine(
                new SingleBlockReaction(event.block.position, PermissionFlag.ENTER)
        )

    }
}
