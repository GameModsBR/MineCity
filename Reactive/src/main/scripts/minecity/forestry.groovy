package minecity

import forestry.apiculture.blocks.BlockBeeHives
import minecity.harvestcraft.PamBeeHive
import minecity.minecraft.Door

blockType(BlockBeeHives) {
    setReactive new PamBeeHive()
}

blockType('forestry:greenhouse.door') {
    setReactive new Door()
}
