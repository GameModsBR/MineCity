package minecity.forestry

import forestry.apiculture.blocks.BlockBeeHives
import minecity.minecraft.Door

blockType(BlockBeeHives) {
    setReactive new ForestryBeeHive()
}

blockType('forestry:greenhouse.door') {
    setReactive new Door()
}
