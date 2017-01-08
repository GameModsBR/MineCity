package minecity

import minecity.multistorage.EnderLinkChest
import shetiphian.endertanks.common.block.BlockEnderTank

blockType(BlockEnderTank) {
    setReactive new EnderLinkChest()
}
