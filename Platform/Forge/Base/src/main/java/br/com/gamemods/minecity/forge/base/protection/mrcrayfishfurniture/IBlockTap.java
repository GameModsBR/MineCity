package br.com.gamemods.minecity.forge.base.protection.mrcrayfishfurniture;

import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.forge.base.accessors.block.IBlock;
import br.com.gamemods.minecity.forge.base.accessors.block.IState;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.accessors.world.IWorldServer;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.ModInterfacesTransformer;
import br.com.gamemods.minecity.forge.base.protection.reaction.*;

import java.util.ArrayList;
import java.util.List;

@Referenced(at = ModInterfacesTransformer.class)
public interface IBlockTap extends IBlock
{
    @Override
    default Reaction reactRightClick(BlockPos pos, IState state, IEntityPlayerMP player, IItemStack stack,
                                     boolean offHand, Direction face)
    {
        IWorldServer world = pos.world.getInstance(IWorldServer.class);
        BlockPos water = pos.add(Direction.DOWN, 2);
        if(!world.getIBlock(water).getUnlocalizedName().equals("tile.water"))
            return NoReaction.INSTANCE;

        BlockPos source = null;
        int metadata = state.getIntValueOrMeta("metadata");
        switch(metadata)
        {
            case 3:
                source = pos.add(-1,0,0);
                break;
            case 1:
                source = pos.add(1,0,0);
                break;
            case 2:
                source = pos.add(0,0,1);
                break;
            case 0:
                source = pos.add(0,0,-1);
                break;
        }

        Reaction reaction;
        if(source != null)
            reaction = new DoubleBlockReaction(PermissionFlag.MODIFY, water, source);
        else
        {
            List<BlockPos> list = new ArrayList<>(5);
            list.add(water);
            Direction.cardinal.forEach(dir-> list.add(pos.add(dir)));
            reaction = new MultiBlockReaction(PermissionFlag.MODIFY, list);
        }

        return new SingleBlockReaction(pos, PermissionFlag.CLICK).combine(
                new BlameOtherInheritedReaction(player.getServer().mineCity, pos, reaction)
        );
    }
}
