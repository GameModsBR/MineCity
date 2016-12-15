package br.com.gamemods.minecity.forge.base.protection.mrcrayfishfurniture;

import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.forge.base.accessors.block.IState;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.ModInterfacesTransformer;
import br.com.gamemods.minecity.forge.base.protection.reaction.ForgeSingleBlockReaction;
import br.com.gamemods.minecity.reactive.reaction.Reaction;

@Referenced(at = ModInterfacesTransformer.class)
public interface IBlockSitRecolor extends IBlockSittable
{
    @Override
    default Reaction reactRightClick(BlockPos pos, IState state, IEntityPlayerMP player, IItemStack stack,
                                     boolean offHand, Direction face)
    {
        if(stack != null && stack.getIItem().getUnlocalizedName().equals("item.dyePowder"))
        {
            ForgeSingleBlockReaction reaction = new ForgeSingleBlockReaction(pos, PermissionFlag.MODIFY);
            reaction.onDenyUpdateBlockAndTileForced(player);
            return reaction;
        }

        return IBlockSittable.super.reactRightClick(pos, state, player, stack, offHand, face);
    }
}
