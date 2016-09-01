package br.com.gamemods.minecity.forge.mc_1_10_2.protection.vanilla;

import br.com.gamemods.minecity.forge.base.accessors.block.IState;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.protection.vanilla.BlockProtections;
import br.com.gamemods.minecity.forge.mc_1_10_2.FrostUtil;
import br.com.gamemods.minecity.forge.mc_1_10_2.MineCityFrost;
import br.com.gamemods.minecity.forge.mc_1_10_2.event.BlockGrowEvent;
import br.com.gamemods.minecity.forge.mc_1_10_2.event.PlayerTeleportDragonEggEvent;
import net.minecraft.util.EnumHand;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;

public class FrostBlockProtections extends BlockProtections
{
    private final MineCityFrost mod;

    public FrostBlockProtections(MineCityFrost mod)
    {
        super(mod);
        this.mod = mod;
    }

    @SuppressWarnings("unchecked")
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onBlockGrow(BlockGrowEvent event)
    {
        if(event.getWorld().isRemote)
            return;

        if(onBlockGrow(
                (IState) event.getState(),
                mod.block(event.getWorld(), event.getPos()),
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
        if(event.getWorld().isRemote)
            return;

        if(onDragonEggTeleport(
                (IEntityPlayerMP) event.player,
                (IState) event.getState(),
                mod.block(event.getWorld(), event.getPos()),
                (List) event.changes
        ))
        {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onItemRightClick(PlayerInteractEvent.RightClickItem event)
    {
        if(event.getWorld().isRemote)
            return;

        if(onItemRightClick(event.getEntityPlayer(), event.getItemStack(), event.getHand() == EnumHand.OFF_HAND))
            event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockEvent.PlaceEvent event)
    {
        if(event.getWorld().isRemote)
            return;

        if(onBlockPlace(event.getPlayer(), event.getBlockSnapshot()))
            event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockEvent.BreakEvent event)
    {
        if(event.getWorld().isRemote)
            return;

        if(onBlockBreak(event.getPlayer(), (IState) event.getState(), mod.block(event.getWorld(), event.getPos())))
            event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onBlockMultiPlace(BlockEvent.MultiPlaceEvent event)
    {
        if(event.getWorld().isRemote)
            return;

        if(onBlockMultiPlace(event.getPlayer(), mod.block(event.getWorld(), event.getPos()), event.getReplacedBlockSnapshots()))
            event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onPlayerRightClickBlock(PlayerInteractEvent.RightClickBlock event)
    {
        if(event.getWorld().isRemote)
            return;

        int result = onPlayerRightClickBlock(
                event.getEntityPlayer(),
                event.getHand() == EnumHand.OFF_HAND,
                event.getItemStack(),
                (IState) event.getWorld().getBlockState(event.getPos()),
                mod.block(event.getWorld(), event.getPos()),
                FrostUtil.toDirection(event.getFace())
        );

        if(result == 3)
            event.setCanceled(true);
        else if(result == 1)
        {
            event.setUseItem(Event.Result.DENY);
            event.setCanceled(true);
        }
        else if(result == 2)
            event.setUseBlock(Event.Result.DENY);
    }
}
