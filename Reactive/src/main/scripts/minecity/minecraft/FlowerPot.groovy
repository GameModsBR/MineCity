package minecity.minecraft

import br.com.gamemods.minecity.api.permission.PermissionFlag
import br.com.gamemods.minecity.reactive.game.block.*
import br.com.gamemods.minecity.reactive.reaction.*

class FlowerPot implements ReactiveBlockType {

    @Override
    Reaction reactRightClick(Interaction event) {
        if(event.blockState.getTraitString('minecraft:flower_pot_contents') == 'empty')
            new SingleBlockReaction(event.block.position, PermissionFlag.MODIFY)
                .onDenySendBlockUpdateAt(event.block.position)
                .onDenySendInventoryUpdate()
        else
            NoReaction.INSTANCE
    }
}
