package minecity

import com.sb205.missing_pieces.MpBlocks.BlockChair
import minecity.minecraft.SimpleReaction

import static br.com.gamemods.minecity.api.permission.PermissionFlag.ENTER
import static br.com.gamemods.minecity.reactive.game.block.data.BlockRole.CLICKABLE

blockType(BlockChair) {
    setReactive new SimpleReaction(blockRole: CLICKABLE, rightClick: ENTER)
}
