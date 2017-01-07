package minecity

import forestry.apiculture.blocks.BlockBeeHives
import minecity.harvestcraft.PamBeeHive

blockType(BlockBeeHives) {
    setReactive new PamBeeHive()
}
