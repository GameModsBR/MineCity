package br.com.gamemods.minecity.reactive.game.block;

import br.com.gamemods.minecity.api.shape.PrecisePoint;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.reactive.game.entity.data.EntityData;
import br.com.gamemods.minecity.reactive.game.entity.data.Hand;
import br.com.gamemods.minecity.reactive.game.item.ReactiveItemStack;
import br.com.gamemods.minecity.reactive.reaction.InteractReaction;
import org.jetbrains.annotations.Nullable;

/**
 * An object that reacts to events related to a block
 */
public interface ReactiveBlockProperty
{
    /**
     * Determine the reaction that would happen when an entity right clicks it
     * @param reaction The current reaction
     * @param entity Who right clicked
     * @param hand Hand used by the character
     * @param stack Stack in hand
     * @param block The block that is reacting
     * @param face The face which was right clicked
     * @param point The point in the block that was right clicked. {@code null} when not supported by the server.
     */
    default void reactRightClick(InteractReaction reaction, EntityData entity, Hand hand, ReactiveItemStack stack,
                                 ReactiveBlock block, Direction face, @Nullable PrecisePoint point)
    {
    }

    /**
     * Determine the reaction that would happen when an entity left clicks it
     * @param reaction The current reaction
     * @param entity Who left clicked
     * @param hand Hand used by the character
     * @param stack Stack in hand
     * @param block The block that is reacting
     * @param face The face which was left clicked
     * @param point The point in the block that was left clicked. {@code null} when not supported by the server.
     */
    default void reactLeftClick(InteractReaction reaction, EntityData entity, Hand hand, ReactiveItemStack stack,
                                ReactiveBlock block, Direction face, @Nullable PrecisePoint point)
    {
    }
}
