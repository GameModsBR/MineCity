package minecity.forge

import br.com.gamemods.minecity.reactive.game.block.data.BlockRole
import minecity.minecraft.CheckTileInventory
import net.minecraft.block.Block

blockType(Block) {
    if(!reactiveBlockType.filter{ it != decorativeBlock }.isPresent())
        setReactive new CheckTileInventory(blockRole: BlockRole.DECORATIVE)
}
