package br.com.gamemods.minecity.forge.base.protection.vanilla;

import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.permission.FlagHolder;
import br.com.gamemods.minecity.api.permission.Permissible;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.accessors.IRayTraceResult;
import br.com.gamemods.minecity.forge.base.accessors.block.IBlockSnapshot;
import br.com.gamemods.minecity.forge.base.accessors.block.IState;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.item.IItem;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.accessors.world.IWorldServer;
import br.com.gamemods.minecity.forge.base.command.ForgePlayer;
import br.com.gamemods.minecity.forge.base.protection.reaction.MultiBlockReaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.Reaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.SingleBlockReaction;
import br.com.gamemods.minecity.structure.ClaimedChunk;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.BlockSnapshot;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class BlockProtections extends ForgeProtections
{
    private IEntityPlayerMP boneMealPlayer;

    public BlockProtections(MineCityForge mod)
    {
        super(mod);
    }

    public boolean onPistonMove(BlockPos pos, IState state, Direction dir, boolean extend, List<IBlockSnapshot> changes, Object movedBy)
    {
        ClaimedChunk last = mod.mineCity.provideChunk(pos.getChunk());

        Permissible permissible;
        if(movedBy instanceof Permissible)
            permissible = (Permissible) movedBy;
        else
            permissible = last.getFlagHolder(pos).owner();

        return new MultiBlockReaction(PermissionFlag.MODIFY,
                changes.stream().map(snap-> snap.getPosition(mod)).collect(Collectors.toList())
        ).can(mod.mineCity, permissible).isPresent();
    }

    public boolean onFillBucket(IEntityPlayerMP entityPlayer, IWorldServer world, IRayTraceResult target, IItemStack bucket, boolean offHand)
    {
        ForgePlayer player = mod.player(entityPlayer);
        Reaction reaction = bucket.getIItem().reactFillBucket(entityPlayer, world, target, bucket, offHand);
        Optional<Message> denial = reaction.can(mod.mineCity, player);
        if(denial.isPresent())
        {
            player.send(FlagHolder.wrapDeny(denial.get()));
            return true;
        }

        return false;
    }

    public boolean onBoneMeal(IEntityPlayerMP entity, BlockPos pos, IState state)
    {
        ForgePlayer player = mod.player(entity);
        Reaction reaction = state.getIBlock().reactBoneMeal(entity, pos, state);

        Optional<Message> denial = reaction.can(mod.mineCity, player);
        if(denial.isPresent())
        {
            boneMealPlayer = null;
            player.send(FlagHolder.wrapDeny(denial.get()));
            return true;
        }

        boneMealPlayer = entity;
        return false;
    }

    public boolean onItemRightClick(EntityPlayer entity, ItemStack itemStack, boolean offHand)
    {
        IItemStack stack = (IItemStack) (Object) itemStack;
        ForgePlayer player = mod.player(entity);
        Reaction reaction = stack.getIItem().reactRightClick((IEntityPlayerMP) entity, stack, offHand);

        Optional<Message> denial = reaction.can(mod.mineCity, player);
        if(denial.isPresent())
        {
            player.send(FlagHolder.wrapDeny(denial.get()));
            return true;
        }

        return false;
    }

    public boolean onBlockGrow(IState state, BlockPos block, List<IBlockSnapshot> changes)
    {
        IEntityPlayerMP boneMealPlayer = this.boneMealPlayer;
        this.boneMealPlayer = null;

        int size = changes.size();
        if(size == 1 && changes.get(0).getPosition(mod).equals(block))
            return false;
        else if(size == 0)
            return false;

        Permissible owner = boneMealPlayer != null? boneMealPlayer : mod.mineCity.provideChunk(block.getChunk()).getFlagHolder(block).owner();

        Reaction reaction = state.getIBlock().reactBlockGrow(mod, state, block, changes, boneMealPlayer);

        Optional<Message> denial = reaction.can(mod.mineCity, owner);
        if(denial.isPresent())
        {
            owner.send(FlagHolder.wrapDeny(denial.get()));
            return true;
        }

        return false;
    }

    public boolean onDragonEggTeleport(IEntityPlayerMP player, IState state, BlockPos pos, List<IBlockSnapshot> changes)
    {
        int size = changes.size();
        if(size == 0)
            return false;

        Reaction react;
        if(size == 1)
            react = new SingleBlockReaction(changes.get(0).getPosition(mod), PermissionFlag.MODIFY);
        else
            react = new MultiBlockReaction(PermissionFlag.MODIFY, changes.stream().map(snap -> snap.getPosition(mod)).collect(Collectors.toList()));

        return react.can(mod.mineCity, player).isPresent();
    }

    public boolean onBlockPlace(EntityPlayer entity, BlockSnapshot snapshot, IItemStack hand, boolean offHand)
    {
        IBlockSnapshot snap = (IBlockSnapshot) snapshot;
        ForgePlayer player = mod.player(entity);
        Reaction reaction = snap.getCurrentState().getIBlock().reactBlockPlace(player, snap, hand, offHand);

        Optional<Message> denial = reaction.can(mod.mineCity, player);
        if(denial.isPresent())
        {
            player.send(FlagHolder.wrapDeny(denial.get()));
            return true;
        }

        return false;
    }

    public boolean onBlockBreak(EntityPlayer entity, IState state, BlockPos pos)
    {
        ForgePlayer player = mod.player(entity);
        Reaction reaction = state.getIBlock().reactBlockBreak(player, state, pos);

        Optional<Message> denial = reaction.can(mod.mineCity, player);
        if(denial.isPresent())
        {
            player.send(FlagHolder.wrapDeny(denial.get()));
            return true;
        }

        return false;
    }

    @SuppressWarnings("unchecked")
    public boolean onBlockMultiPlace(EntityPlayer entity, BlockPos blockPos, Collection<BlockSnapshot> replacedBlocks)
    {
        ForgePlayer player = mod.player(entity);
        ClaimedChunk chunk = null;
        for(IBlockSnapshot state : (Collection<IBlockSnapshot>) (Collection) replacedBlocks)
        {
            blockPos = new BlockPos(blockPos, state.getX(), state.getY(), state.getZ());
            chunk = mod.mineCity.provideChunk(blockPos.getChunk(), chunk);
            FlagHolder holder = chunk.getFlagHolder(blockPos);
            Optional<Message> denial = holder.can(player, PermissionFlag.MODIFY);
            if(denial.isPresent())
            {
                player.send(FlagHolder.wrapDeny(denial.get()));
                return true;
            }
        }

        return false;
    }

    public int onPlayerRightClickBlock(EntityPlayer entity, boolean offHand, ItemStack itemStack, IState state, BlockPos pos, Direction face, boolean verbose)
    {
        byte result = 0;

        ForgePlayer player = mod.player(entity);
        player.offHand = offHand;
        IItemStack stack = mod.stack(itemStack);
        Optional<Message> denial;
        if(stack != null)
        {
            IItem item = stack.getIItem();
            Reaction reaction = item.reactRightClickBlockFirstUse((IEntityPlayerMP) entity, stack, offHand, state, pos, face);
            denial = reaction.can(mod.mineCity, player);
            if(denial.isPresent())
                result = 3;

            reaction = item.reactRightClickBlock((IEntityPlayerMP) entity, stack, offHand, state, pos, face);
            Optional<Message> denial2 = reaction.can(mod.mineCity, player);
            if(denial2.isPresent())
            {
                result |= 1;
                if(!denial.isPresent())
                    denial = denial2;
            }
        }
        else denial = Optional.empty();

        Reaction reaction = state.getIBlock().reactRightClick(pos, state, (IEntityPlayerMP) entity, stack, offHand,face);
        Optional<Message> denial2 = reaction.can(mod.mineCity, player);
        if(denial2.isPresent())
            result |= 2;

        Message message = denial.orElse(denial2.orElse(null));
        if(verbose && message != null)
            player.send(FlagHolder.wrapDeny(message));

        return result;
    }
}
