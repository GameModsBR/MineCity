package br.com.gamemods.minecity.reactive.vanilla.block;

import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.shape.Cuboid;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.reactive.game.block.Modification;
import br.com.gamemods.minecity.reactive.game.block.ReactiveBlockProperty;
import br.com.gamemods.minecity.reactive.game.block.data.BlockStateData;
import br.com.gamemods.minecity.reactive.game.block.data.BlockTypeData;
import br.com.gamemods.minecity.reactive.game.server.data.ChunkData;
import br.com.gamemods.minecity.reactive.reaction.NoReaction;
import br.com.gamemods.minecity.reactive.reaction.Reaction;
import br.com.gamemods.minecity.reactive.reaction.ShapeBlockReaction;

public interface GravityBlock extends ReactiveBlockProperty
{
    default boolean willFallOnPlacement(ChunkData chunk, BlockPos pos)
    {
        BlockTypeData blockType = chunk.getBlockTypeData(pos.add(Direction.DOWN));

        return blockType.matches("minecraft:air")
                || blockType.matches("minecraft:fire")
                || blockType.matches("minecraft:water")
                || blockType.matches("minecraft:flowing_water")
                || blockType.matches("minecraft:lava")
                || blockType.matches("minecraft:flowing_lava")
        ;
    }

    default boolean willFallThrough(ChunkData chunk, BlockPos pos)
    {
        BlockStateData state = chunk.getBlockStateData(pos);
        BlockTypeData blockType = state.getBlockTypeData();

        return blockType.matches("minecraft:air")
                || blockType.matches("minecraft:fire")
                || blockType.matches("minecraft:water")
                || blockType.matches("minecraft:flowing_water")
                || blockType.matches("minecraft:lava")
                || blockType.matches("minecraft:flowing_lava")
                || state.isReplaceable()
        ;
    }

    default Reaction reactFallOnPlacement(ChunkData chunk, BlockPos pos)
    {
        final BlockPos max = pos.add(Direction.DOWN);
        BlockPos min = pos;
        BlockPos cur = pos;
        while ((cur = cur.add(Direction.DOWN)).y >= 0 && willFallThrough(chunk, cur))
            min = cur;

        return new ShapeBlockReaction(pos.world, new Cuboid(min, max), PermissionFlag.MODIFY);
    }

    @Override
    default Reaction reactPlaceOne(Modification event)
    {
        ChunkData chunk = event.getChunk();
        BlockPos pos = event.getPosition();

        if(!willFallOnPlacement(chunk, pos))
            return NoReaction.INSTANCE;

        return reactFallOnPlacement(chunk, pos);
    }
}
