package minecity.biomesoplenty

import br.com.gamemods.minecity.api.permission.PermissionFlag
import br.com.gamemods.minecity.reactive.game.block.*
import br.com.gamemods.minecity.reactive.reaction.*

class BOPPlant implements ReactiveBlockType {
    @Override
    Reaction reactRightClick(Interaction event) {
        if(event.blockState.getTraitString('variant') == 'berrybush')
            new SingleBlockReaction(event.block.position, PermissionFlag.HARVEST)
        else
            NoReaction.INSTANCE
    }
}
