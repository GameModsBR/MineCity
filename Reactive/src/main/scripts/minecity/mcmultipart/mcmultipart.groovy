package minecity.mcmultipart

import mcmultipart.item.ItemMultiPart

itemType(ItemMultiPart) {
    setReactive new ItemPart()
}

blockType('mcmultipart:multipart') {
    setReactive new MultiPart()
}
