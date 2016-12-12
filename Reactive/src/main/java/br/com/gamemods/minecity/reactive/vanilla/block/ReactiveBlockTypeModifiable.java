package br.com.gamemods.minecity.reactive.vanilla.block;

import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.shape.Point;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.reactive.game.block.ReactiveBlock;
import br.com.gamemods.minecity.reactive.game.block.ReactiveBlockType;
import br.com.gamemods.minecity.reactive.game.block.data.BlockRole;
import br.com.gamemods.minecity.reactive.game.entity.ReactiveEntity;
import br.com.gamemods.minecity.reactive.game.entity.data.Hand;
import br.com.gamemods.minecity.reactive.game.item.ReactiveItemStack;
import br.com.gamemods.minecity.reactive.reaction.InteractReaction;
import br.com.gamemods.minecity.reactive.reaction.SingleBlockReaction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A block that structurally changes on right clicks. Like repeaters, you can break a redstone circuit when you
 * right click it.
 */
public interface ReactiveBlockTypeModifiable extends ReactiveBlockType
{
    @NotNull
    @Override
    default BlockRole getBlockRole()
    {
        return BlockRole.MODIFIABLE;
    }

    @Override
    default void reactRightClick(InteractReaction reaction, ReactiveEntity entity, Hand hand,
                                 ReactiveItemStack stack, ReactiveBlock block, Direction face,
                                 @Nullable Point point)
    {
        reaction.combineBlock(new SingleBlockReaction(block.getPosition(), PermissionFlag.MODIFY));
    }
}
