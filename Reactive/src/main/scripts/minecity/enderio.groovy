package minecity

import minecity.minecraft.CheckTileInventory

blockType('crazypants.enderio.BlockEio') {
    setReactive new CheckTileInventory()
}
