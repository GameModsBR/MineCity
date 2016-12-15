package minecity.minecraft

import br.com.gamemods.minecity.api.permission.PermissionFlag
import br.com.gamemods.minecity.reactive.game.block.*
import br.com.gamemods.minecity.reactive.reaction.*

/**
 * Check for MODIFY permission when a block is right clicked with a FlintAndSteel in hand
 */
class ReactiveTNT implements ReactiveBlockType {
    @Override
    Reaction reactRightClick(Interaction event) {

        if(event.stack.itemData.matches('minecraft:flint_and_steel'))
            new SingleBlockReaction(event.block.position, PermissionFlag.MODIFY)
        else
            NoReaction.INSTANCE

    }
}
