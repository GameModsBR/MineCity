package minecity.minecraft

import br.com.gamemods.minecity.api.permission.PermissionFlag
import br.com.gamemods.minecity.reactive.game.block.Interaction
import br.com.gamemods.minecity.reactive.game.item.ReactiveItem
import br.com.gamemods.minecity.reactive.reaction.*

class EnderEye implements ReactiveItem {

    @Override
    Reaction reactRightClickLast(Interaction event) {

        if(event.blockTypeData.matches('minecraft:end_portal_frame') && event.blockState.getTraitString('eye') == 'false')
            new SingleBlockReaction(event.block.position, PermissionFlag.MODIFY)
        else
            NoReaction.INSTANCE

    }
}
