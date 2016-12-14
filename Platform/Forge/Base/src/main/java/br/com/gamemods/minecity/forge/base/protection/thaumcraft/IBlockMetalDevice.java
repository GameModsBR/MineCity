package br.com.gamemods.minecity.forge.base.protection.thaumcraft;

import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.forge.base.accessors.block.IBlockOpenReactor;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.ModInterfacesTransformer;
import br.com.gamemods.minecity.forge.base.protection.reaction.DoubleBlockReaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.Reaction;

@Referenced(at = ModInterfacesTransformer.class)
public interface IBlockMetalDevice extends IBlockOpenReactor, IBlockPlaceMetaReaction
{
    @Override
    default Reaction reactPlace(BlockPos pos, int meta)
    {
        switch(meta)
        {
            case 2:
                return new DoubleBlockReaction(PermissionFlag.MODIFY, pos, pos.add(Direction.DOWN));
        }

        return null;
    }
}
