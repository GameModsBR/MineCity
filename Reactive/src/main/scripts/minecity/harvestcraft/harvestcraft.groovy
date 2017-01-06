package minecity.harvestcraft

import com.pam.harvestcraft.blocks.blocks.BlockPamCake
import com.pam.harvestcraft.blocks.growables.*
import minecity.minecraft.Cake
import net.minecraft.block.BlockCrops

blockType(BlockPamCake) {
    setReactive new Cake()
}

blockType([BlockCrops, BlockPamFruit, BlockPamFruitLog]) {
    setReactive new PamRightClickHarvesting()
}
