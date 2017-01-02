package minecity.minecraft

import br.com.gamemods.minecity.api.permission.PermissionFlag
import br.com.gamemods.minecity.api.world.Direction
import br.com.gamemods.minecity.reactive.game.block.Interaction
import br.com.gamemods.minecity.reactive.reaction.*
import br.com.gamemods.minecity.reactive.vanilla.block.ReactiveBlockClickable

class Door implements ReactiveBlockClickable {

    @Override
    Reaction reactRightClick(Interaction event) {
        def reaction = new SingleBlockReaction(event.block.position, PermissionFlag.CLICK)
        if(event.block.blockStateData.getTraitString('minecraft:door_half') == "upper")
            reaction.onDenySendBlockUpdateAt(event.block.position.add(Direction.DOWN))
        return reaction
    }
}
