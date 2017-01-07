package minecity.harvestcraft

import com.pam.harvestcraft.blocks.blocks.*
import com.pam.harvestcraft.blocks.growables.*
import minecity.minecraft.*
import net.minecraft.block.BlockCrops

blockType(BlockPamCake) {
    setReactive new Cake()
}

blockType([BlockCrops, BlockPamFruit]) {
    setReactive(
            reactiveBlockType.filter{ !(it instanceof CheckTileInventory) }.map{it as PamRightClickHarvesting}
            .orElseGet{
                def age = blockTypeData.traits.find { it.name == 'age' && it.valueClass == Integer }
                if(blockType instanceof PamCropGrowable)
                    new PamCrop(age: age, grownAge: blockType.matureAge)
                else
                    new Crop(age: age, grownAge: (int) age?.possibleValues?.max() ?: -1) as PamRightClickHarvesting
            }
    )
}

blockType(BlockPamFruitLog) {
    setReactive new PamFruitLog()
}

blockType(BlockBaseGarden) {
    setReactive new PamGarden()
}
