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
 * A block that can store things inside it and open its storage on right click.
 */
public interface ReactiveBlockTypeContainer extends ReactiveBlockType
{
    ReactiveBlockTypeContainer INSTANCE = new ReactiveBlockTypeContainer(){};

    @NotNull
    @Override
    default BlockRole getBlockRole()
    {
        return BlockRole.CONTAINER;
    }

    @Override
    default void reactRightClick(InteractReaction reaction, ReactiveEntity entity, Hand hand,
                                 ReactiveItemStack stack, ReactiveBlock block, Direction face,
                                 @Nullable Point point)
    {
        reaction.combineBlock(new SingleBlockReaction(block.getPosition(), PermissionFlag.OPEN));
    }
}
