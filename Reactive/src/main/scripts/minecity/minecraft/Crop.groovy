package minecity.minecraft

import br.com.gamemods.minecity.api.command.Message
import br.com.gamemods.minecity.reactive.game.block.*
import br.com.gamemods.minecity.reactive.game.block.data.*
import br.com.gamemods.minecity.reactive.reaction.*

import static br.com.gamemods.minecity.api.permission.PermissionFlag.*

class Crop implements ReactiveBlockType {

    BlockRole blockRole = BlockRole.HARVESTEABLE
    BlockTraitData age
    int grownAge = -1

    boolean isMature(BlockStateData state) {
        age? state.getTrait(age).filter{ it == grownAge }.isPresent() : false
    }

    @Override
    Reaction reactBeingBroken(Modification event) {
        def state = event.blockChange.original.blockStateData
        def seeds = age? state.withTrait(age, 0).orElse(null) : null

        def chunk = event.chunk
        def pos = event.position

        if(!seeds || !age)
            new SingleBlockReaction(pos, MODIFY)

        else if(isMature(state))
            new SingleBlockReaction(pos, HARVEST).onAllowExecNextTick {
                chunk.setBlock pos, seeds
            }
        else
            new SingleBlockReaction(pos, MODIFY)
                .onDenySendDenialMessage HARVEST, new Message('action.harvest.not-fully-grown', 'This plant is not fully grown yet.')
    }
}
