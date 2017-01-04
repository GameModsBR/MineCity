package minecity

import br.com.gamemods.minecity.reactive.game.block.data.BlockRole
import minecity.minecraft.CheckTileInventory
import net.minecraft.block.Block

blockType(Block) {
    if(!reactiveBlockType.isPresent())
        setReactive new CheckTileInventory(blockRole: BlockRole.DECORATIVE)
}
