package minecity.harvestcraft

import com.pam.harvestcraft.blocks.blocks.BlockPamCake
import com.pam.harvestcraft.blocks.growables.*
import minecity.minecraft.*
import net.minecraft.block.BlockCrops

blockType(BlockPamCake) {
    setReactive new Cake()
}

blockType(BlockCrops) {
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

class PamFruit implements PamRightClickHarvesting {

}

blockType([BlockPamFruit, BlockPamFruitLog]) {
    setReactive new PamFruit()
}
