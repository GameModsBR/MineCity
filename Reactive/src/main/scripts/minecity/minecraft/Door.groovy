package minecity.minecraft

import br.com.gamemods.minecity.api.permission.PermissionFlag
import br.com.gamemods.minecity.api.world.Direction
import br.com.gamemods.minecity.reactive.game.block.Interaction
import br.com.gamemods.minecity.reactive.reaction.*
import br.com.gamemods.minecity.reactive.vanilla.block.ReactiveBlockClickable

class Door implements ReactiveBlockClickable {

    @Override
    Reaction reactRightClick(Interaction event) {
        new SingleBlockReaction(event.block.position, PermissionFlag.CLICK).with {
            if(event.block.blockStateData.getTraitString('minecraft:door_half') == "upper")
                onDenySendBlockUpdateAt(event.block.position.add(Direction.DOWN))
            else delegate
        }
    }
}
