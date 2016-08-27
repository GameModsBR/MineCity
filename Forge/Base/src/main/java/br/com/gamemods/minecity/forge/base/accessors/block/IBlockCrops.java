package br.com.gamemods.minecity.forge.base.accessors.block;

import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.accessors.entity.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.item.IItem;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.accessors.world.IWorldServer;
import br.com.gamemods.minecity.forge.base.command.ForgePlayer;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.block.BlockCropsTransformer;
import br.com.gamemods.minecity.forge.base.protection.reaction.DenyReaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.Reaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.SingleBlockReaction;
import net.minecraft.block.BlockCrops;

@Referenced(at = BlockCropsTransformer.class)
public interface IBlockCrops extends IBlock
{
    @Override
    default BlockCrops getForgeBlock()
    {
        return (BlockCrops) this;
    }

    default IItem getISeed()
    {
        BlockCrops crops = (BlockCrops) this;
        return (IItem) crops.getItemDropped(crops.getStateFromMeta(0), null, 0);
    }

    default int getMaxAge()
    {
        return getForgeBlock().getMaxAge();
    }

    @Override
    default Reaction reactBlockPlace(ForgePlayer<?, ?, ?> player, IBlockSnapshot snap)
    {
        IItemStack stack = player.cmd.sender.getStackInHand(player.offHand);
        if(stack != null && stack.getItem() != getISeed())
            return new SingleBlockReaction(snap.getPosition(player.getServer()), PermissionFlag.HARVEST);

        return new SingleBlockReaction(snap.getPosition(player.getServer()), PermissionFlag.MODIFY);
    }

    @Override
    default Reaction reactBlockBreak(ForgePlayer<?, ?, ?> player, IState state, BlockPos pos)
    {
        assert pos.world.instance != null;
        IEntityPlayerMP entity = (IEntityPlayerMP) player.player;
        if(entity.isCreative())
        {
            if(!entity.isSneaking())
                return new DenyReaction(new Message(
                        "action.harvest-on-creative",
                        "You can't harvest plants on creative mode, that would just reset the growth state without any drop."
                ));
        }
        else if(state.getIntValueOrMeta("age") == getMaxAge())
        {
            SingleBlockReaction reaction = new SingleBlockReaction(pos, PermissionFlag.HARVEST);
            MineCityForge mod = player.getServer();
            reaction.addAllowListener((r, permissible, flag, p, message) ->
            {
                mod.consumeItemsOrAddOwner(p.precise(), 2, 1, 2, getISeed(), player.getUniqueId());
                mod.callSyncMethod(() ->
                        ((IWorldServer) pos.world.instance).setBlock(pos, getDefaultIState())
                );
            });
            return reaction;
        }

        return new SingleBlockReaction(pos, PermissionFlag.MODIFY);
    }
}
