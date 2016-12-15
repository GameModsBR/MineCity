package br.com.gamemods.minecity.forge.base.accessors.block;

import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.item.IItem;
import br.com.gamemods.minecity.forge.base.accessors.world.IWorldServer;
import br.com.gamemods.minecity.forge.base.command.ForgePlayer;
import br.com.gamemods.minecity.reactive.reaction.DenyReaction;
import br.com.gamemods.minecity.reactive.reaction.Reaction;
import br.com.gamemods.minecity.reactive.reaction.SingleBlockReaction;

public interface SimpleCrop extends IBlock
{
    default IItem getISeed(IWorldServer world)
    {
        return getItemDropped(getDefaultIState(), null, 0);
    }

    default boolean isHarvestAge(int age)
    {
        return age == 7;
    }

    default boolean shouldReplant(int age)
    {
        return isHarvestAge(age);
    }

    @Override
    default Reaction reactBlockBreak(ForgePlayer<?, ?, ?> player, IState state, BlockPos pos)
    {
        IEntityPlayerMP entity = (IEntityPlayerMP) player.player;
        if(entity.isCreative())
        {
            if(!entity.isSneaking())
                return new DenyReaction(new Message(
                        "action.harvest-on-creative",
                        "You can't harvest plants on creative mode, that would just reset the growth state without any drop."
                ));
        }
        else
        {
            int age = state.getIntValueOrMeta("age");
            if(isHarvestAge(age))
            {
                SingleBlockReaction reaction = new SingleBlockReaction(pos, PermissionFlag.HARVEST);
                MineCityForge mod = player.getServer();
                reaction.addAllowListener((r, permissible, flag, p, message) ->
                {
                    mod.consumeItemsOrAddOwnerIf(p.precise(), 2, 1, 2, getISeed(player.cmd.sender.getIWorld()), player.identity(), item->
                            item.getStack().getIItem().isHarvest(item.getStack())
                    );
                    if(shouldReplant(age))
                        mod.callSyncMethod(() ->
                                pos.world.getInstance(IWorldServer.class).setBlock(pos, getDefaultIState())
                        );
                });
                return reaction;
            }
        }

        return new SingleBlockReaction(pos, PermissionFlag.MODIFY);
    }

    @Override
    default boolean isHarvest()
    {
        return true;
    }
}
