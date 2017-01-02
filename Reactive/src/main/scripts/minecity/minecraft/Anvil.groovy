package minecity.minecraft

import br.com.gamemods.minecity.reactive.game.block.data.BlockRole
import br.com.gamemods.minecity.reactive.vanilla.block.*

class Anvil implements ReactiveBlockContainer, GravityBlock {

    @Override
    BlockRole getBlockRole() {
        BlockRole.CONTAINER
    }

}
