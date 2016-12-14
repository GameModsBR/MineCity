package minecity.minecraft

import br.com.gamemods.minecity.api.permission.PermissionFlag
import br.com.gamemods.minecity.api.shape.PrecisePoint
import br.com.gamemods.minecity.api.world.Direction
import br.com.gamemods.minecity.reactive.game.block.*
import br.com.gamemods.minecity.reactive.game.entity.data.*
import br.com.gamemods.minecity.reactive.game.item.ReactiveItemStack
import br.com.gamemods.minecity.reactive.reaction.*
import org.jetbrains.annotations.Nullable

class ReactiveBed implements ReactiveBlockType {
    @Override
    void reactRightClick(InteractReaction reaction, EntityData entity, Hand hand, ReactiveItemStack stack,
                         ReactiveBlock block, Direction face, @Nullable PrecisePoint point) {

        reaction.combineBlock(
                new SingleBlockReaction(block.position, PermissionFlag.CLICK).combine(
                        new SingleBlockReaction(block.position, PermissionFlag.ENTER)
                )
        )

    }
}
