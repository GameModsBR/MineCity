package minecity.harvestcraft

import com.pam.harvestcraft.blocks.blocks.BlockPamCake
import com.pam.harvestcraft.blocks.growables.*
import minecity.minecraft.Cake

blockType(BlockPamCake) {
    setReactive new Cake()
}

blockType([BlockPamCrop, BlockPamFruit, BlockPamFruitLog]) {
    setReactive new PamRightClickHarvesting()
}
