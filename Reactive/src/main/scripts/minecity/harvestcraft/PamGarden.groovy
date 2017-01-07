package minecity.harvestcraft

import br.com.gamemods.minecity.api.command.Message
import br.com.gamemods.minecity.api.shape.Cuboid
import br.com.gamemods.minecity.reactive.game.block.*
import br.com.gamemods.minecity.reactive.game.block.data.BlockRole
import br.com.gamemods.minecity.reactive.reaction.*
import com.pam.harvestcraft.HarvestCraft

import static br.com.gamemods.minecity.api.permission.PermissionFlag.*

class PamGarden implements ReactiveBlockType {

    BlockRole blockRole = BlockRole.HARVESTEABLE

    @Override
    Reaction reactBeingBroken(Modification event) {
        if(!HarvestCraft.config.enablegardenSpread)
            return new SingleBlockReaction(event.position, MODIFY)

        def center = event.position
        def chunk = event.chunk
        def state = event.blockChange.original.blockStateData
        def spreadArea = new Cuboid(center.subtract(3, 1, 3), center.add(3, 1, 3))
        def found = spreadArea.blockIterator(center.world).count { chunk.getBlockStateData(it) == state }

        if(found < Math.min(3, HarvestCraft.config.gardenSpreadMax))
            new SingleBlockReaction(event.position, MODIFY)
                    .onDenySendDenialMessage HARVEST, new Message('action.harvest.not-fully-grown', 'This plant is not fully grown yet.')
        else
            new SingleBlockReaction(event.position, HARVEST)
    }
}
