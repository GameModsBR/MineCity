package br.com.gamemods.minecity.forge.mc_1_7_10.protection.vanilla;

import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.accessors.block.IState;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.protection.vanilla.BlockProtections;
import br.com.gamemods.minecity.forge.mc_1_7_10.SevenUtil;
import br.com.gamemods.minecity.forge.mc_1_7_10.accessors.block.SevenBlockState;
import br.com.gamemods.minecity.forge.mc_1_7_10.event.BlockGrowEvent;
import br.com.gamemods.minecity.forge.mc_1_7_10.event.PlayerTeleportDragonEggEvent;
import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.block.Block;
import net.minecraftforge.event.entity.player.BonemealEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;

import java.util.List;

public class SevenBlockProtections extends BlockProtections
{
    private final MineCityForge mod;

    public SevenBlockProtections(MineCityForge mod)
    {
        super(mod);
        this.mod = mod;
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onBoneMeal(BonemealEvent event)
    {
        if(event.world.isRemote)
            return;

        if(onBoneMeal(
                (IEntityPlayerMP) event.entityPlayer,
                new BlockPos(mod.world(event.world), event.x, event.y, event.z),
                state(event.block, event.world.getBlockMetadata(event.x, event.y, event.z))
        ))
        {
            event.setCanceled(true);
        }
    }

    @SuppressWarnings("unchecked")
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onBlockGrow(BlockGrowEvent event)
    {
        if(event.world.isRemote)
            return;

        if(onBlockGrow(
                state(event.block, event.blockMetadata),
                new BlockPos(mod.world(event.world), event.x, event.y, event.z),
                (List) event.changes
        ))
        {
            event.setCanceled(true);
        }
    }

    @SuppressWarnings("unchecked")
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onDragonEggTeleport(PlayerTeleportDragonEggEvent event)
    {
        if(event.world.isRemote)
            return;

        if(onDragonEggTeleport(
                (IEntityPlayerMP) event.player,
                state(event.block, event.blockMetadata),
                new BlockPos(mod.world(event.world), event.x, event.y, event.z),
                (List) event.changes
        ))
        {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockEvent.PlaceEvent event)
    {
        if(event.world.isRemote)
            return;

        if(onBlockPlace(event.player, event.blockSnapshot))
            event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockEvent.BreakEvent event)
    {
        if(event.world.isRemote)
            return;

        if(onBlockBreak(
                event.getPlayer(),
                state(event.block, event.blockMetadata),
                new BlockPos(mod.world(event.world), event.x, event.y, event.z)
        ))
        {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onBlockMultiPlace(BlockEvent.MultiPlaceEvent event)
    {
        if(event.world.isRemote)
            return;

        if(onBlockMultiPlace(
                event.player,
                new BlockPos(mod.world(event.world), event.x, event.y, event.z),
                event.getReplacedBlockSnapshots()
        ))
        {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        if(event.world.isRemote)
            return;

        if(event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK)
        {
            int result = onPlayerRightClickBlock(
                    event.entityPlayer, false, event.entityPlayer.getHeldItem(),
                    state(event.world.getBlock(event.x, event.y, event.z), event.world.getBlockMetadata(event.x, event.y, event.z)),
                    new BlockPos(mod.world(event.world), event.x, event.y, event.z),
                    SevenUtil.toDirection(event.face)
            );

            if(result == 3)
                event.setCanceled(true);
            else if(result == 1)
                event.useItem = Event.Result.DENY;
            else if(result == 2)
                event.useBlock = Event.Result.DENY;
        }
        else if(event.action == PlayerInteractEvent.Action.RIGHT_CLICK_AIR)
        {
            if(onItemRightClick(event.entityPlayer, event.entityPlayer.getHeldItem(), false))
                event.setCanceled(true);
        }
    }

    private IState state(Block block, int meta)
    {
        return new SevenBlockState(block, meta);
    }
}
