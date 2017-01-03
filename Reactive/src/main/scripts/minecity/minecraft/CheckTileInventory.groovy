package minecity.minecraft

import br.com.gamemods.minecity.api.permission.PermissionFlag
import br.com.gamemods.minecity.reactive.game.block.*
import br.com.gamemods.minecity.reactive.game.block.data.BlockRole
import br.com.gamemods.minecity.reactive.reaction.*

class CheckTileInventory implements ReactiveBlockType {

    BlockRole role
    PermissionFlag flag

    CheckTileInventory(BlockRole role = BlockRole.CONTAINER, PermissionFlag flag = PermissionFlag.OPEN) {
        this.role = role
        this.flag = flag
    }

    @Override
    BlockRole getBlockRole() {
        role
    }

    @Override
    Reaction reactRightClick(Interaction event) {
        def tile = event.block.tileEntity
        if(!tile.isPresent() || !(tile.get().tileEntity instanceof net.minecraft.inventory.IInventory))
            return NoReaction.INSTANCE

        return new SingleBlockReaction(event.block.position, flag)
    }
}