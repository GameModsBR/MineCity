package minecity.minecraft

import br.com.gamemods.minecity.reactive.game.block.Interaction
import br.com.gamemods.minecity.reactive.game.item.ReactiveItem
import br.com.gamemods.minecity.reactive.reaction.Reaction

class Record implements ReactiveItem {

    @Override
    Reaction reactRightClickLast(Interaction event) {
        event.reactionBlock.useBlock
    }
}
