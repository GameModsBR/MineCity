package br.com.gamemods.minecity.forge.mc_1_10_2.protection.vanilla;

import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.permission.FlagHolder;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.forge.base.protections.vanilla.ForgeProtections;
import br.com.gamemods.minecity.forge.mc_1_10_2.MineCityFrost;
import br.com.gamemods.minecity.forge.mc_1_10_2.command.FrostPlayer;
import br.com.gamemods.minecity.structure.ClaimedChunk;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAnvil;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.IGrowable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Optional;

public class FrostBlockProtections extends ForgeProtections
{
    private final MineCityFrost mod;

    public FrostBlockProtections(MineCityFrost mod)
    {
        super(mod);
        this.mod = mod;
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockEvent.PlaceEvent event)
    {
        if(event.getWorld().isRemote)
            return;

        BlockPos pos = mod.block(event.getWorld(), event.getPos());
        ClaimedChunk chunk = mod.mineCity.provideChunk(pos.getChunk());
        FlagHolder holder = chunk.getFlagHolder(pos);

        FrostPlayer player = mod.player(event.getPlayer());
        Optional<Message> denial = holder.can(player, PermissionFlag.MODIFY);
        if(denial.isPresent())
        {
            event.setCanceled(true);
            player.send(FlagHolder.wrapDeny(denial.get()));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockEvent.BreakEvent event)
    {
        if(event.getWorld().isRemote)
            return;

        BlockPos pos = mod.block(event.getWorld(), event.getPos());
        ClaimedChunk chunk = mod.mineCity.provideChunk(pos.getChunk());
        FlagHolder holder = chunk.getFlagHolder(pos);

        FrostPlayer player = mod.player(event.getPlayer());
        Optional<Message> denial = holder.can(player, PermissionFlag.MODIFY);
        if(denial.isPresent())
        {
            event.setCanceled(true);
            player.send(FlagHolder.wrapDeny(denial.get()));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onBlockMultiPlace(BlockEvent.MultiPlaceEvent event)
    {
        if(event.getWorld().isRemote)
            return;

        FrostPlayer player = mod.player(event.getPlayer());
        BlockPos blockPos = mod.block(event.getWorld(), event.getPos());
        ClaimedChunk chunk = null;
        for(BlockSnapshot state : event.getReplacedBlockSnapshots())
        {
            blockPos = mod.block(blockPos, state.getPos());
            chunk = mod.mineCity.provideChunk(blockPos.getChunk(), chunk);
            FlagHolder holder = chunk.getFlagHolder(blockPos);
            Optional<Message> denial = holder.can(player, PermissionFlag.MODIFY);
            if(denial.isPresent())
            {
                event.setCanceled(true);
                player.send(FlagHolder.wrapDeny(denial.get()));
                return;
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onPlayerRightClickBlock(PlayerInteractEvent.RightClickBlock event)
    {
        if(event.getWorld().isRemote)
            return;

        World world = event.getWorld();
        IBlockState state = world.getBlockState(event.getPos());
        Block block = state.getBlock();
        EntityPlayer entityPlayer = event.getEntityPlayer();
        ItemStack item = entityPlayer.getHeldItem(EnumHand.MAIN_HAND);
        if(item != null && item.getItem() == Items.DYE && item.getItemDamage() == 15)
        {
            if(block instanceof IGrowable)
            {
                if(check(mod.block(event.getWorld(), event.getPos()), entityPlayer, PermissionFlag.MODIFY))
                    event.setCanceled(true);
                return;
            }
        }

        if(block instanceof BlockContainer || block instanceof BlockAnvil)
        {
            if(check(mod.block(event.getWorld(), event.getPos()), entityPlayer, PermissionFlag.OPEN))
                event.setCanceled(true);
        }
    }
}
