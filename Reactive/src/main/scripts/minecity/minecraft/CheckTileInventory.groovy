package minecity.minecraft

import br.com.gamemods.minecity.api.permission.PermissionFlag
import br.com.gamemods.minecity.reactive.game.block.*
import br.com.gamemods.minecity.reactive.game.block.data.BlockRole
import br.com.gamemods.minecity.reactive.reaction.*
import net.minecraft.inventory.IInventory

class CheckTileInventory implements ReactiveBlockType {

    BlockRole blockRole = BlockRole.CONTAINER
    PermissionFlag flag = PermissionFlag.OPEN

    @Override
    Reaction reactRightClick(Interaction event) {
        event.block.tileEntity.with {
            if(filter{ it.tileEntity instanceof IInventory }.isPresent())
                new SingleBlockReaction(event.block.position, flag)
            else
                NoReaction.INSTANCE
        }
    }
}