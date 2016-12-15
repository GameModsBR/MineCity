package minecity.minecraft

import br.com.gamemods.minecity.reactive.game.block.data.BlockRole
import br.com.gamemods.minecity.reactive.vanilla.block.*

class Anvil implements ReactiveBlockTypeContainer, GravityBlock {

    @Override
    BlockRole getBlockRole() {
        BlockRole.CONTAINER
    }

}
