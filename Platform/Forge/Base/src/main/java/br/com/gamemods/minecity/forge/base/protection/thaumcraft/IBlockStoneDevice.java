package br.com.gamemods.minecity.forge.base.protection.thaumcraft;

import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.forge.base.accessors.block.IBlockOpenReactor;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.ModInterfacesTransformer;
import br.com.gamemods.minecity.reactive.reaction.DoubleBlockReaction;
import br.com.gamemods.minecity.reactive.reaction.MultiBlockReaction;
import br.com.gamemods.minecity.reactive.reaction.Reaction;

import java.util.Arrays;

@Referenced(at = ModInterfacesTransformer.class)
public interface IBlockStoneDevice extends IBlockOpenReactor, IBlockPlaceMetaReaction
{
    @Override
    default Reaction reactPlace(BlockPos pos, int meta)
    {
        switch(meta)
        {
            case 0:
            case 5:
            case 9:
            case 10:
            case 12:
                return new DoubleBlockReaction(PermissionFlag.MODIFY, pos, pos.add(Direction.UP));
            case 11:
                return MultiBlockReaction.create(PermissionFlag.MODIFY, Arrays.asList(pos, pos.add(Direction.DOWN), pos.add(Direction.DOWN, 2)));
        }

        return null;
    }
}
