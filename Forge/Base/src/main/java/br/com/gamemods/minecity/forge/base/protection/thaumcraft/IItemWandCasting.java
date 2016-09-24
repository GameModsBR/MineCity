package br.com.gamemods.minecity.forge.base.protection.thaumcraft;

import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.forge.base.accessors.IRayTraceResult;
import br.com.gamemods.minecity.forge.base.accessors.block.IBlock;
import br.com.gamemods.minecity.forge.base.accessors.block.IState;
import br.com.gamemods.minecity.forge.base.accessors.block.ITileEntity;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityLivingBase;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.item.IItem;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.accessors.world.IWorldServer;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.ModInterfacesTransformer;
import br.com.gamemods.minecity.forge.base.protection.reaction.NoReaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.Reaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.SingleBlockReaction;
import net.minecraft.nbt.NBTTagCompound;

import java.util.concurrent.atomic.AtomicBoolean;

@Referenced(at = ModInterfacesTransformer.class)
public interface IItemWandCasting extends IItem
{
    @Override
    default Reaction reactRightClickBlockFirstUse(IEntityPlayerMP player, IItemStack stack, boolean offHand,
                                                  IState state, BlockPos pos, Direction face)
    {
        IBlock block = state.getIBlock();
        AtomicBoolean finalize = new AtomicBoolean();
        Reaction reaction = NoReaction.INSTANCE;
        if(block instanceof Wandable)
        {
            Wandable wandable = (Wandable) block;
            reaction = wandable.reactWandRightClick(player, stack, offHand, state, pos, face, finalize);
            if(finalize.get())
                return reaction;
        }

        IWorldServer world = pos.world.getInstance(IWorldServer.class);
        ITileEntity tile = world.getTileEntity(pos);
        if(tile instanceof Wandable)
        {
            Wandable wandable = (Wandable) tile;
            reaction = reaction.combine(wandable.reactWandRightClick(player, stack, offHand, state, pos, face, finalize));
            if(finalize.get())
                return reaction;
        }

        if(WandTriggerManager.hasTriggers(state))
            return reaction.combine(Reaction.combine(
                    WandTriggerManager.getPerformTriggerReactions(player, stack, pos, face, state)
            ));

        int md = state.getIntValueOrMeta("metadata");
        if( ( (block instanceof IBlockWoodenDevice || block instanceof IBlockCosmeticOpaque) && md == 2)
            && (!ThaumHooks.getConfigWardedStone() || tile != null && tile instanceof ITileOwned && player.getName().equals(((ITileOwned)tile).getOwnerName()))
        )
        {
            return new SingleBlockReaction(pos, PermissionFlag.MODIFY);
        }

        if(block instanceof IBlockArcaneDoor && (!ThaumHooks.getConfigWardedStone() || tile != null && tile instanceof ITileOwned && player.getName().equals(((ITileOwned)tile).getOwnerName())))
        {
            return new SingleBlockReaction(pos, PermissionFlag.MODIFY);
        }

        return reaction;
    }

    @Override
    default Reaction reactRightClick(IEntityPlayerMP player, IItemStack stack, boolean offHand)
    {
        IWorldServer world = player.getIWorld();
        IRayTraceResult result = rayTrace(world, player, true);
        Reaction reaction = NoReaction.INSTANCE;
        if(result != null && result.getHitType() == 1)
        {
            BlockPos hit = result.getHitBlockPos(player.getServer().world(world));
            IState state = world.getIState(hit);
            IBlock block = state.getIBlock();

            AtomicBoolean finalize = new AtomicBoolean();
            if(block instanceof Wandable)
            {
                Wandable wandable = (Wandable) block;
                reaction = wandable.reactWandRightClick(world, stack, player, hit, finalize);
                if(finalize.get())
                    return reaction;
            }

            ITileEntity tile = world.getTileEntity(hit);
            if(tile instanceof Wandable)
            {
                Wandable wandable = (Wandable) tile;
                reaction = reaction.combine(wandable.reactWandRightClick(world, stack, player, hit, finalize));
                if(finalize.get())
                    return reaction;
            }
        }

        IItemFocusBasic focus = getFocus(stack);
        if(focus != null && !ThaumHooks.isOnWandCooldown(player))
            return reaction.combine(focus.reactFocusRightClick(stack, world, player, result));

        return reaction;
    }

    default IItemFocusBasic getFocus(IItemStack stack)
    {
        return ThaumHooks.getFocus(this, stack);
    }

    default Wandable getObjectInUse(IItemStack stack, IWorldServer world)
    {
        NBTTagCompound tag = stack.getTag();
        if(tag != null && tag.hasKey("IIUX"))
        {
            ITileEntity tile = world.getTileEntity(tag.getInteger("IIUX"), tag.getInteger("IIUY"), tag.getInteger("IIUZ"));
            if(tile instanceof Wandable)
                return (Wandable) tile;
        }

        return null;
    }

    @Override
    default Reaction reactItemUseTick(IEntityPlayerMP player, IItemStack stack, int count)
    {
        Wandable wandable = getObjectInUse(stack, player.getIWorld());
        if(wandable != null)
            return wandable.onUsingWandTick(stack, player, count);

        IItemFocusBasic focus = getFocus(stack);
        if(focus != null && !ThaumHooks.isOnWandCooldown(player))
            return focus.onUsingFocusTick(stack, player, count);

        return NoReaction.INSTANCE;
    }

    @Override
    default Reaction reactLivingSwing(IEntityLivingBase living, IItemStack stack)
    {
        IItemFocusBasic focus = getFocus(stack);
        if(focus == null || ThaumHooks.isOnWandCooldown(living))
            return NoReaction.INSTANCE;

        return focus.reactLivingSwing(living, stack);
    }
}
