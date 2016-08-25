package br.com.gamemods.minecity.forge.mc_1_7_10.protection.vanilla;

import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.permission.FlagHolder;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.forge.base.command.ForgePlayer;
import br.com.gamemods.minecity.forge.base.protections.vanilla.ForgeProtections;
import br.com.gamemods.minecity.forge.mc_1_7_10.MineCitySeven;
import br.com.gamemods.minecity.structure.ClaimedChunk;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAnvil;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.IGrowable;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;

import java.util.Optional;

public class SevenBlockProtections extends ForgeProtections
{
    private final MineCitySeven mod;

    public SevenBlockProtections(MineCitySeven mod)
    {
        super(mod);
        this.mod = mod;
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockEvent.PlaceEvent event)
    {
        if(event.world.isRemote)
            return;

        BlockPos pos = new BlockPos(mod.world(event.world), event.x, event.y, event.z);
        ClaimedChunk chunk = mod.mineCity.provideChunk(pos.getChunk());
        FlagHolder holder = chunk.getFlagHolder(pos);

        ForgePlayer player = mod.player(event.player);
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
        if(event.world.isRemote)
            return;

        BlockPos pos = new BlockPos(mod.world(event.world), event.x, event.y, event.z);
        ClaimedChunk chunk = mod.mineCity.provideChunk(pos.getChunk());
        FlagHolder holder = chunk.getFlagHolder(pos);

        ForgePlayer player = mod.player(event.getPlayer());
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
        if(event.world.isRemote)
            return;

        ForgePlayer player = mod.player(event.player);
        BlockPos blockPos = new BlockPos(mod.world(event.world), event.x, event.y, event.z);
        ClaimedChunk chunk = null;
        for(BlockSnapshot state : event.getReplacedBlockSnapshots())
        {
            blockPos = new BlockPos(blockPos, state.x, state.y, state.z);
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
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        if(event.world.isRemote)
            return;

        if(event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK)
        {
            Block block = event.world.getBlock(event.x, event.y, event.z);
            ItemStack item = event.entityPlayer.getHeldItem();
            if(item != null && item.getItem() == Items.dye && item.getItemDamage() == 15)
            {
                if(block instanceof IGrowable)
                {
                    if(check(new BlockPos(mod.world(event.world), event.x, event.y, event.z), event.entityPlayer, PermissionFlag.MODIFY))
                        event.setCanceled(true);
                    return;
                }
            }

            if(block instanceof BlockContainer || block instanceof BlockAnvil)
            {
                if(check(new BlockPos(mod.world(event.world), event.x, event.y, event.z), event.entityPlayer, PermissionFlag.OPEN))
                    event.setCanceled(true);
            }
        }
    }
}
