package minecity.harvestcraft

import br.com.gamemods.minecity.api.permission.PermissionFlag
import br.com.gamemods.minecity.reactive.game.block.*
import br.com.gamemods.minecity.reactive.game.block.data.BlockRole
import br.com.gamemods.minecity.reactive.reaction.*
import com.pam.harvestcraft.HarvestCraft

trait PamRightClickHarvesting implements ReactiveBlockType {

    BlockRole blockRole = BlockRole.HARVESTEABLE

    @Override
    Reaction reactRightClick(Interaction event) {
        if(HarvestCraft.config.enableEasyHarvest)
            event.reactionBlock.combineAction(new SingleBlockReaction(event.block.position, PermissionFlag.HARVEST))
        NoReaction.INSTANCE
    }
}
