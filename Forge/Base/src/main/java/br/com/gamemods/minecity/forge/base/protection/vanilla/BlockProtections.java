package br.com.gamemods.minecity.forge.base.protection.vanilla;

import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.permission.FlagHolder;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.accessors.block.IBlockSnapshot;
import br.com.gamemods.minecity.forge.base.accessors.block.IState;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.command.ForgePlayer;
import br.com.gamemods.minecity.forge.base.protection.reaction.Reaction;
import br.com.gamemods.minecity.structure.ClaimedChunk;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.BlockSnapshot;

import java.util.Collection;
import java.util.Optional;

public class BlockProtections extends ForgeProtections
{
    public BlockProtections(MineCityForge mod)
    {
        super(mod);
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

    public boolean onBlockPlace(EntityPlayer entity, BlockSnapshot snapshot)
    {
        IBlockSnapshot snap = (IBlockSnapshot) snapshot;
        ForgePlayer player = mod.player(entity);
        Reaction reaction = snap.getReplacedState().getIBlock().reactBlockPlace(player, snap);

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

    public int onPlayerRightClickBlock(EntityPlayer entity, boolean offHand, ItemStack itemStack, IState state, BlockPos pos, Direction face)
    {
        byte result = 0;

        ForgePlayer player = mod.player(entity);
        player.offHand = offHand;
        IItemStack stack = mod.stack(itemStack);
        Optional<Message> denial;
        if(stack != null)
        {
            Reaction reaction = stack.getIItem().reactRightClickBlock((IEntityPlayerMP) entity, stack, offHand, state, pos, face);
            denial = reaction.can(mod.mineCity, player);
            if(denial.isPresent())
                result = 1;
        }
        else denial = Optional.empty();

        Reaction reaction = state.getIBlock().reactRightClick(pos, state, (IEntityPlayerMP) entity, stack, offHand,face);
        Optional<Message> denial2 = reaction.can(mod.mineCity, player);
        if(denial2.isPresent())
            result |= 2;

        Message message = denial.orElse(denial2.orElse(null));
        if(message != null)
            player.send(FlagHolder.wrapDeny(message));

        return result;
    }
}
