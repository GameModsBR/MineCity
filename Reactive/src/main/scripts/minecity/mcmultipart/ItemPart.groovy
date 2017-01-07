package minecity.mcmultipart

import br.com.gamemods.minecity.api.permission.PermissionFlag
import br.com.gamemods.minecity.reactive.game.block.Interaction
import br.com.gamemods.minecity.reactive.game.item.ReactiveItem
import br.com.gamemods.minecity.reactive.reaction.*

class ItemPart implements ReactiveItem {
    @Override
    Reaction reactRightClickFirst(Interaction event) {
        if(event.hasBlock())
            new BlockAndSidesReaction(PermissionFlag.MODIFY, event.block.position)
        else
            NoReaction.INSTANCE
    }
}
