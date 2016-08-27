package br.com.gamemods.minecity.forge.base.accessors.block;

import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.EntityPos;
import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.accessors.entity.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.item.IItem;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.accessors.world.IWorldServer;
import br.com.gamemods.minecity.forge.base.command.ForgePlayer;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.block.BlockCropsTransformer;
import br.com.gamemods.minecity.forge.base.protection.reaction.Reaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.SingleBlockReaction;
import net.minecraft.block.BlockCrops;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraftforge.common.util.Constants;

import java.util.concurrent.atomic.AtomicBoolean;

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
        //TODO: Notification message "action.harvest-on-creative"
        IEntityPlayerMP entity = (IEntityPlayerMP) player.player;
        if(!entity.isCreative() && state.getIntValueOrMeta("age") == getMaxAge())
        {
            SingleBlockReaction reaction = new SingleBlockReaction(pos, PermissionFlag.HARVEST);
            MineCityForge mod = player.getServer();
            reaction.addAllowListener((r, permissible, flag, p, message) ->
            {
                AtomicBoolean consume = new AtomicBoolean(true);
                IItem seed = getISeed();

                mod.addSpawnListener(e -> {
                    if(e instanceof EntityItem)
                        mod.callSyncMethod(()->
                        {
                            EntityPos entityPos = e.getEntityPos(mod);
                            double distance = entityPos.distance(pos);
                            if(distance <= 2)
                            {
                                EntityItem item = (EntityItem) e;
                                if(consume.get())
                                {
                                    IItemStack stack = (IItemStack) (Object) item.getEntityItem();
                                    if(stack.getIItem() == seed)
                                    {
                                        int size = stack.getSize();
                                        if(size == 1)
                                        {
                                            consume.set(false);
                                            item.setDead();
                                            return;
                                        }
                                        else if(size > 1)
                                        {
                                            consume.set(false);
                                            stack.setSize(size--);
                                        }
                                        else
                                            item.setDead();
                                    }
                                }

                                NBTTagCompound nbt = item.getEntityData();
                                NBTTagList allow = nbt.getTagList("MineCityAllowPickup", Constants.NBT.TAG_STRING);
                                allow.appendTag(new NBTTagString(player.getUniqueId().toString()));
                                nbt.setTag("MineCityAllowPickup", allow);
                            }
                        });
                    return false;
                }, 2);
                mod.callSyncMethod(() ->
                        ((IWorldServer) pos.world.instance).setBlock(pos, getDefaultIState())
                );
            });
            return reaction;
        }

        return new SingleBlockReaction(pos, PermissionFlag.MODIFY);
    }
}
