package br.com.gamemods.minecity.forge.base.protection.thaumcraft;

import br.com.gamemods.minecity.api.permission.Permissible;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.forge.base.accessors.block.IBlockOpenReactor;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.ModInterfacesTransformer;
import br.com.gamemods.minecity.reactive.reaction.BlockAndSidesReaction;
import br.com.gamemods.minecity.reactive.reaction.Reaction;

@Referenced(at = ModInterfacesTransformer.class)
public interface IBlockTube extends IBlockOpenReactor
{
    @Override
    default Reaction reactPrePlace(Permissible who, IItemStack stack, BlockPos pos)
    {
        return new BlockAndSidesReaction(PermissionFlag.MODIFY, pos);
    }
}
