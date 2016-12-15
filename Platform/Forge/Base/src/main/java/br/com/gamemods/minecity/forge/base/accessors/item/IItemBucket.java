package br.com.gamemods.minecity.forge.base.accessors.item;

import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.forge.base.accessors.IRayTraceResult;
import br.com.gamemods.minecity.forge.base.accessors.block.IBlock;
import br.com.gamemods.minecity.forge.base.accessors.block.IState;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.world.IWorldServer;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.item.ItemBucketTransformer;
import br.com.gamemods.minecity.reactive.reaction.ObservedReaction;
import br.com.gamemods.minecity.reactive.reaction.Reaction;

@Referenced(at = ItemBucketTransformer.class)
public interface IItemBucket extends IItem
{
    @Referenced(at = ItemBucketTransformer.class)
    IBlock getLiquidBlock();

    @Override
    default Reaction reactFillBucket(IEntityPlayerMP player, IWorldServer world, IRayTraceResult target,
                                     IItemStack bucket, boolean offHand)
    {
        IBlock liquid = getLiquidBlock();

        final BlockPos pos;
        Reaction reaction = null;
        int hitType = target.getHitType();
        if(hitType == 1)
        {
            BlockPos placePos = target.getHitBlockPos(player.getServer().world(world));
            if(!liquid.getUnlocalizedName().equals("tile.air"))
            {
                pos = placePos.add(target.getHitSide());
                reaction = liquid.reactPrePlace(player, bucket, placePos);
            }
            else
                pos = placePos;
        }
        else if(hitType == 2)
            pos = target.getEntity().getBlockPos(player.getServer());
        else
            pos = player.getBlockPos();

        if(reaction == null)
        {
            IState state = world.getIState(pos);
            if(liquid.getUnlocalizedName().equals("tile.air"))
                reaction = state.getIBlock().reactBlockBreak(player.getMineCityPlayer(), state, pos);
            else
                reaction = state.getIBlock().reactPrePlace(player, bucket, pos);
        }

        return new ObservedReaction(reaction).addDenyListener(message -> {
            player.sendInventoryContents();
            player.sendBlockAndTile(pos);
        });
    }
}
