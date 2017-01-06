package minecity.minecraft

import br.com.gamemods.minecity.api.permission.PermissionFlag
import br.com.gamemods.minecity.api.world.Direction
import br.com.gamemods.minecity.reactive.game.block.*
import br.com.gamemods.minecity.reactive.game.block.data.BlockRole
import br.com.gamemods.minecity.reactive.reaction.*

class Melon implements ReactiveBlockType {

    BlockRole blockRole = BlockRole.HARVESTEABLE
    String stem = 'minecraft:melon_stem'

    @Override
    Reaction reactBeingBroken(Modification event) {
        def pos = event.position
        def chunk = event.chunk

        def flag = Direction.cardinal.stream().map {
            chunk.getBlockTypeData(pos.add(it))
        }.anyMatch {
            it.matches(stem)
        }? PermissionFlag.HARVEST : PermissionFlag.MODIFY


        new SingleBlockReaction(event.position, flag)
    }
}
