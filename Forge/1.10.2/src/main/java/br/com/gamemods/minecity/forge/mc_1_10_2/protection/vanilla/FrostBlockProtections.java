package br.com.gamemods.minecity.forge.mc_1_10_2.protection.vanilla;

import br.com.gamemods.minecity.forge.base.accessors.IRayTraceResult;
import br.com.gamemods.minecity.forge.base.accessors.block.IState;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.accessors.world.IWorldServer;
import br.com.gamemods.minecity.forge.base.command.ForgePlayer;
import br.com.gamemods.minecity.forge.base.protection.vanilla.BlockProtections;
import br.com.gamemods.minecity.forge.mc_1_10_2.FrostUtil;
import br.com.gamemods.minecity.forge.mc_1_10_2.MineCityFrost;
import br.com.gamemods.minecity.forge.mc_1_10_2.event.BlockGrowEvent;
import br.com.gamemods.minecity.forge.mc_1_10_2.event.PistonMoveEvent;
import br.com.gamemods.minecity.forge.mc_1_10_2.event.PlayerTeleportDragonEggEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraftforge.event.entity.player.BonemealEvent;
import net.minecraftforge.event.entity.player.FillBucketEvent;
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

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onPistonMove(PistonMoveEvent event)
    {
        if(event.getWorld().isRemote)
            return;

        if(onPistonMove(
                mod.block(event.getWorld(), event.getPos()),
                (IState) event.getState(),
                FrostUtil.toDirection(event.direction),
                event.extend,
                event.changes,
                event.movedBy
        ))
        {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onFillBucket(FillBucketEvent event)
    {
        if(event.getWorld().isRemote)
            return;

        EntityPlayer entityPlayer = event.getEntityPlayer();
        ForgePlayer player = mod.player(entityPlayer);

        if(onFillBucket(
                (IEntityPlayerMP) entityPlayer,
                (IWorldServer) event.getWorld(),
                (IRayTraceResult) event.getTarget(),
                (IItemStack) (Object) (player.offHand? entityPlayer.getHeldItemOffhand() : entityPlayer.getHeldItemMainhand()),
                player.offHand
        ))
        {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onBoneMeal(BonemealEvent event)
    {
        if(event.getWorld().isRemote)
            return;

        if(onBoneMeal(
                (IEntityPlayerMP) event.getEntityPlayer(),
                mod.block(event.getWorld(), event.getPos()),
                (IState) event.getBlock()
        ))
        {
            event.setCanceled(true);
        }
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

        EntityPlayer entity = event.getPlayer();
        ForgePlayer player = mod.player(entity);
        IItemStack stack = (IItemStack) (Object) (player.offHand? entity.getHeldItemOffhand() : event.getItemInHand());
        if(onBlockPlace(entity, event.getBlockSnapshot(), stack, player.offHand))
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
