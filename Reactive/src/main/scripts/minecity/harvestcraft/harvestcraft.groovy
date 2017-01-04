package minecity.harvestcraft

import com.pam.harvestcraft.blocks.blocks.BlockPamCake
import com.pam.harvestcraft.blocks.growables.BlockPamCrop
import minecity.minecraft.Cake

blockType(BlockPamCake) {
    setReactive new Cake()
}

blockType(BlockPamCrop) {
    setReactive new PamCrop()
}
