package minecity

import edgarallen.mods.scf.blocks.common.BlockBaseFrame
import minecity.minecraft.SimpleReaction

import static br.com.gamemods.minecity.api.permission.PermissionFlag.OPEN
import static br.com.gamemods.minecity.reactive.game.block.data.BlockRole.CLICKABLE

blockType(BlockBaseFrame) {
    setReactive new SimpleReaction(blockRole: CLICKABLE, rightClick: OPEN, leftClick: OPEN)
}
