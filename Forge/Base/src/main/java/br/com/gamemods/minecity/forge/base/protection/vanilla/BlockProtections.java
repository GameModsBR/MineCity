package br.com.gamemods.minecity.forge.base.protection.vanilla;

import br.com.gamemods.minecity.api.CollectionUtil;
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
import br.com.gamemods.minecity.forge.base.protection.reaction.NoReaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.Reaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.SingleBlockReaction;
import br.com.gamemods.minecity.structure.ClaimedChunk;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.BlockSnapshot;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
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

    public Reaction onBlockPlaceLogic(ForgePlayer<?,?,?> player, BlockSnapshot snapshot, IItemStack hand, boolean offHand)
    {
        IBlockSnapshot snap = (IBlockSnapshot) snapshot;
        Reaction reaction = snap.getCurrentState().getIBlock().reactBlockPlace(player, snap, hand, offHand);
        if(hand != null)
            return reaction.combine(hand.getIItem().reactBlockPlace(player.cmd.sender, hand, offHand, snap));
        return reaction;
    }

    public boolean onBlockPlace(EntityPlayer entity, BlockSnapshot snapshot, IItemStack hand, boolean offHand)
    {
        ForgePlayer player = mod.player(entity);
        Optional<Message> denial = onBlockPlaceLogic(player, snapshot, hand, offHand).can(mod.mineCity, player);
        if(denial.isPresent())
        {
            player.send(FlagHolder.wrapDeny(denial.get()));
            return true;
        }

        return false;
    }

    public boolean onPlayerCheckEdit(IEntityPlayerMP player, int x, int y, int z, Direction dir, IItemStack stack)
    {
        IWorldServer world = player.getIWorld();
        BlockPos pos = new BlockPos(mod.world(world), x, y, z);
        IItem.PlayerEditAction action;
        if(stack != null)
        {
            IItem item = stack.getIItem();
            action = item.onPlayerCheckEdit(player, pos, dir, stack);
            Reaction reaction;
            switch(action)
            {
                case REACT:
                    reaction = item.reactPlayerCheckEdit(player, pos, dir, stack);
                    break;

                case SIMULATE_PRE_PLACE:
                    reaction = item.reactPrePlace(player, stack, pos);
                    break;

                default:
                    reaction = null;
            }

            if(reaction != null)
            {
                Optional<Message> denial = reaction.can(mod.mineCity, player);
                if(denial.isPresent())
                {
                    player.send(FlagHolder.wrapDeny(denial.get()));
                    return true;
                }

                return false;
            }
        }
        else
        {
            action = IItem.PlayerEditAction.SIMULATE_BREAK;
        }

        switch(action)
        {
            case NOTHING:
                return false;

            default:
                return onBlockBreak((EntityPlayer) player, world.getIState(pos), pos, false);
        }
    }

    public boolean onBlockBreak(EntityPlayer entity, IState state, BlockPos pos)
    {
        return onBlockBreak(entity, state, pos, true);
    }

    public boolean onBlockBreak(EntityPlayer entity, IState state, BlockPos pos, boolean verbose)
    {
        ForgePlayer player = mod.player(entity);
        Reaction reaction = state.getIBlock().reactBlockBreak(player, state, pos);

        Optional<Message> denial = reaction.can(mod.mineCity, player);
        if(denial.isPresent())
        {
            if(verbose)
                player.send(FlagHolder.wrapDeny(denial.get()));
            return true;
        }

        return false;
    }

    @SuppressWarnings("unchecked")
    public Reaction onBlockMultiPlaceLogic(ForgePlayer<?,?,?> player, BlockPos blockPos, List<BlockSnapshot> replacedBlocks, IItemStack hand, boolean offHand)
    {
        List<IBlockSnapshot> snapshots = (List) replacedBlocks;

        Reaction reaction;
        if(hand != null)
            reaction = hand.getIItem().reactBlockMultiPlace(
                    player.cmd.sender, hand, offHand, blockPos, snapshots
            );
        else
            reaction = NoReaction.INSTANCE;

        Set<BlockPos> checkedPos = new HashSet<>((int)(snapshots.size()*0.75));
        AtomicReference<BlockPos> last = new AtomicReference<>(blockPos);
        AtomicReference<Reaction> atomicReaction = new AtomicReference<>(reaction);
        CollectionUtil.reverseStream(snapshots.listIterator()).forEachOrdered(snap-> {
            BlockPos pos = snap.getPosition(mod, last.get());
            last.set(pos);

            if(checkedPos.add(pos))
                atomicReaction.set(atomicReaction.get().combine(
                        snap.getCurrentState().getIBlock().reactBlockPlace(player, snap, hand, offHand)
                ));
        });

        return atomicReaction.get();
    }

    public boolean onBlockMultiPlace(EntityPlayer entity, BlockPos blockPos, List<BlockSnapshot> replacedBlocks, IItemStack hand, boolean offHand)
    {
        ForgePlayer player = mod.player(entity);
        Optional<Message> denial = onBlockMultiPlaceLogic(player, blockPos, replacedBlocks, hand, offHand)
                .can(mod.mineCity, player);
        if(denial.isPresent())
        {
            player.send(FlagHolder.wrapDeny(denial.get()));
            return true;
        }

        return false;
    }

    public int onPlayerLeftClickBlock(IEntityPlayerMP player, IState state, BlockPos pos, Direction face, IItemStack stack, boolean offHand)
    {
        byte result = 0;
        ForgePlayer forgePlayer = mod.player(player);
        forgePlayer.offHand = offHand;
        Optional<Message> denial;
        if(stack == null)
            denial = Optional.empty();
        else
        {
             denial = stack.getIItem().reactLeftClickBlock(player, state, pos, face, stack, offHand)
                    .can(mod.mineCity, player);

            if(denial.isPresent())
                result = 1;
        }

        Reaction reaction = state.getIBlock().reactLeftClick(player, state, pos, face, stack, offHand);
        Optional<Message> denial2 = reaction.can(mod.mineCity, player);
        if(denial2.isPresent())
            result |= 2;

        Message message = denial.orElse(denial2.orElse(null));
        if(message != null)
            player.send(FlagHolder.wrapDeny(message));

        return result;
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
