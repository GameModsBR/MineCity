package minecity.minecraft

import br.com.gamemods.minecity.api.command.Message
import br.com.gamemods.minecity.api.world.Direction
import br.com.gamemods.minecity.reactive.game.block.*
import br.com.gamemods.minecity.reactive.game.block.data.BlockRole
import br.com.gamemods.minecity.reactive.reaction.*

import static br.com.gamemods.minecity.api.permission.PermissionFlag.*

class Cactus implements ReactiveBlockType {

    BlockRole blockRole = BlockRole.HARVESTEABLE

    @Override
    Reaction reactBeingBroken(Modification event) {
        event.chunk.getBlockTypeData(event.position.add(Direction.DOWN)).with {
            if (it == event.blockChange.original.blockTypeData || it.matches('minecraft:air'))
                new SingleBlockReaction(event.position, HARVEST)
            else
                new SingleBlockReaction(event.position, MODIFY)
                    .onDenySendDenialMessage HARVEST, new Message('action.harvest.not-fully-grown', 'This plant is not fully grown yet.')
        }
    }
}
