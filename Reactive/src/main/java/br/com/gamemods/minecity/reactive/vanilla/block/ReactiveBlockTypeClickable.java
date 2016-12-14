package br.com.gamemods.minecity.reactive.vanilla.block;

import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.shape.PrecisePoint;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.reactive.game.block.ReactiveBlock;
import br.com.gamemods.minecity.reactive.game.block.ReactiveBlockType;
import br.com.gamemods.minecity.reactive.game.block.data.BlockRole;
import br.com.gamemods.minecity.reactive.game.entity.data.EntityData;
import br.com.gamemods.minecity.reactive.game.entity.data.Hand;
import br.com.gamemods.minecity.reactive.game.item.ReactiveItemStack;
import br.com.gamemods.minecity.reactive.reaction.InteractReaction;
import br.com.gamemods.minecity.reactive.reaction.SingleBlockReaction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A block that has simple click reactions, like wooden doors and buttons.
 */
public interface ReactiveBlockTypeClickable extends ReactiveBlockType
{
    ReactiveBlockTypeClickable INSTANCE = new ReactiveBlockTypeClickable(){};

    @NotNull
    @Override
    default BlockRole getBlockRole()
    {
        return BlockRole.CLICKABLE;
    }

    @Override
    default void reactRightClick(InteractReaction reaction, EntityData entity, Hand hand,
                                 ReactiveItemStack stack, ReactiveBlock block, Direction face,
                                 @Nullable PrecisePoint point)
    {
        reaction.combineBlock(new SingleBlockReaction(block.getPosition(), PermissionFlag.CLICK));
    }
}
