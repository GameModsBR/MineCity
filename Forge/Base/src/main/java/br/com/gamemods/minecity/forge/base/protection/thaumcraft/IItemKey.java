package br.com.gamemods.minecity.forge.base.protection.thaumcraft;

import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.forge.base.accessors.block.IBlock;
import br.com.gamemods.minecity.forge.base.accessors.block.IState;
import br.com.gamemods.minecity.forge.base.accessors.block.ITileEntity;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.item.IItem;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.accessors.world.IWorldServer;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.ModInterfacesTransformer;
import br.com.gamemods.minecity.forge.base.protection.reaction.ApproveReaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.NoReaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.Reaction;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.IBlockAccess;

@Referenced(at = ModInterfacesTransformer.class)
public interface IItemKey extends IItem
{
    @Override
    default Reaction reactRightClickBlockFirstUse(IEntityPlayerMP player, IItemStack stack, boolean offHand,
                                                  IState state, BlockPos pos, Direction face)
    {
        IWorldServer world = pos.world.getInstance(IWorldServer.class);
        IBlock block = state.getIBlock();
        int meta = state.getIntValueOrMeta("metadata");
        if(!(block instanceof IBlockArcaneDoor) && (!(block instanceof IBlockWoodenDevice) || meta != 2 && meta != 3))
            return NoReaction.INSTANCE;

        int mod = 0;
        int mod2 = 1;
        byte type = 0;
        if(block instanceof IBlockArcaneDoor)
        {
            int loc = ((IBlockArcaneDoor) block).getFullMetadata((IBlockAccess) world, pos.x, pos.y, pos.z);
            if((loc & 8) != 0)
            {
                mod = -1;
                mod2 = 0;
            }
        }
        else
        {
            type = 1;
        }

        ITileEntity tile = world.getTileEntity(pos.x, pos.y+mod, pos.z);
        if(!(tile instanceof ITileOwned))
            return NoReaction.INSTANCE;
        ITileOwned owned = (ITileOwned) tile;

        PlayerID id = player.identity();
        NBTTagCompound tag = stack.getTag();

        owned.isOwner(id);
        owned.hasAccess(id);

        if(tag == null)
            return NoReaction.INSTANCE;

        ITileOwned other;
        if(type == 0)
        {
            tile = world.getTileEntity(pos.x, pos.y + mod2, pos.z);
            if(tile instanceof ITileOwned)
                other = (ITileOwned) tile;
            else
                other = null;
        }
        else
            other = null;

        return new ApproveReaction(pos, PermissionFlag.CLICK).addAllowListener((reaction, permissible, flag, pos1, message) ->
                player.getServer().callSyncMethod(()-> {
                    owned.registerAccess(id);
                    if(other != null)
                        other.registerAccess(id);
                })
        );
    }
}
