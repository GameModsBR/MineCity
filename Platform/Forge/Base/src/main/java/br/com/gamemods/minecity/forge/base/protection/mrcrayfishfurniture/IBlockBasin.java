package br.com.gamemods.minecity.forge.base.protection.mrcrayfishfurniture;

import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.accessors.block.IBlockOpenReactor;
import br.com.gamemods.minecity.forge.base.accessors.block.IState;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.ModInterfacesTransformer;
import br.com.gamemods.minecity.reactive.reaction.BlameOtherInheritedReaction;
import br.com.gamemods.minecity.reactive.reaction.Reaction;
import br.com.gamemods.minecity.reactive.reaction.SingleBlockReaction;

@Referenced(at = ModInterfacesTransformer.class)
public interface IBlockBasin extends IBlockOpenReactor
{
    @Override
    default Reaction reactRightClick(BlockPos pos, IState state, IEntityPlayerMP player, IItemStack stack,
                                     boolean offHand, Direction face)
    {
        MineCityForge mod = player.getServer();
        Reaction reaction = new SingleBlockReaction(pos, PermissionFlag.OPEN);
        BlockPos water = pos.add(Direction.DOWN, 2);
        if(player.getIWorld().getIBlock(water).getUnlocalizedName().equals("tile.water"))
            reaction = reaction.combine(
                new BlameOtherInheritedReaction(
                    mod.mineCity, pos, new SingleBlockReaction(water, PermissionFlag.MODIFY)
                )
            );

        return reaction;
    }
}
