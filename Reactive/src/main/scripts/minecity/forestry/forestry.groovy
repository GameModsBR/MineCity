package minecity.forestry

import forestry.apiculture.blocks.BlockBeeHives
import minecity.forge.Wrench
import minecity.minecraft.Door

blockType(BlockBeeHives) {
    setReactive new ForestryBeeHive()
}

blockType('forestry:greenhouse.door') {
    setReactive new Door()
}

itemType('forestry:wrench') {
    setReactive new Wrench()
}
