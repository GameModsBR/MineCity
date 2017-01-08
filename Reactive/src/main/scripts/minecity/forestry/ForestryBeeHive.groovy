package minecity.forestry

import br.com.gamemods.minecity.api.permission.PermissionFlag
import br.com.gamemods.minecity.reactive.game.block.Modification
import br.com.gamemods.minecity.reactive.reaction.*
import forestry.api.core.IToolScoop
import minecity.harvestcraft.PamBeeHive

class ForestryBeeHive extends PamBeeHive {

    @Override
    Reaction reactBeingBroken(Modification event) {
        if(event.usedStack.filter{ it.itemData.item instanceof IToolScoop }.isPresent())
            return super.reactBeingBroken(event)
        else
            new SingleBlockReaction(event.position, PermissionFlag.MODIFY)
    }
}
