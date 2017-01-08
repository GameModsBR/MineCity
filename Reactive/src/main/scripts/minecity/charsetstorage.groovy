package minecity

import minecity.minecraft.SimpleReaction
import pl.asie.charset.storage.barrel.BlockBarrel

import static br.com.gamemods.minecity.api.permission.PermissionFlag.OPEN
import static br.com.gamemods.minecity.reactive.game.block.data.BlockRole.CONTAINER

blockType(BlockBarrel) {
    setReactive new SimpleReaction(blockRole: CONTAINER, rightClick: OPEN, leftClick: OPEN)
}
