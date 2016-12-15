package br.com.gamemods.minecity.forge.base.protection.appeng;

import br.com.gamemods.minecity.api.permission.Permissible;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.forge.base.accessors.block.IBlock;
import br.com.gamemods.minecity.forge.base.accessors.block.IBlockSnapshot;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemBlock;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.accessors.world.IWorldServer;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.ModInterfacesTransformer;
import br.com.gamemods.minecity.reactive.reaction.Reaction;
import br.com.gamemods.minecity.reactive.reaction.SingleBlockReaction;

@Referenced(at = ModInterfacesTransformer.class)
public interface IAEBaseItemBlock extends IItemBlock
{
    @Override
    default Reaction reactPrePlace(Permissible who, IItemStack stack, BlockPos pos)
    {
        if(stack.getUnlocalizedName().equals("tile.appliedenergistics2.BlockCrank"))
        {
            IWorldServer world = pos.world.getInstance(IWorldServer.class);
            IBlock down = world.getIBlock(pos.x, pos.y - 1, pos.z);
            if(down != null && down.getUnlocalizedName().equals("tile.appliedenergistics2.BlockGrinder"))
                return new SingleBlockReaction(pos, PermissionFlag.OPEN);
        }

        return IItemBlock.super.reactPrePlace(who, stack, pos);
    }

    @Override
    default Reaction reactBlockPlace(IEntityPlayerMP player, IItemStack stack, boolean offHand,
                                     IBlockSnapshot snap)
    {
        return reactPrePlace(player, stack, snap.getPosition(player.getServer()));
    }
}
