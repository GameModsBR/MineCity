package br.com.gamemods.minecity.forge.mc_1_10_2.listeners;

import br.com.gamemods.minecity.forge.base.MineCityForge;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class FrostToolListener extends br.com.gamemods.minecity.forge.base.listeners.ToolListener
{
    public FrostToolListener(MineCityForge forge)
    {
        super(forge);
    }

    @SubscribeEvent
    public void onPlayerRightClickBlock(PlayerInteractEvent.RightClickBlock event)
    {
        onPlayerInteract(event, false);
    }

    @SubscribeEvent
    public void onPlayerLeftClickBlock(PlayerInteractEvent.LeftClickBlock event)
    {
        onPlayerInteract(event, true);
    }

    private void onPlayerInteract(PlayerInteractEvent event, boolean left)
    {
        World world = event.getWorld();
        if(world.isRemote)
            return;

        EntityPlayer player = event.getEntityPlayer();
        BlockPos pos = event.getPos();
        if(onPlayerInteract(
                player, player.getHeldItem(EnumHand.MAIN_HAND),
                world, pos.getX(), pos.getY(), pos.getZ(),
                left
        ))
        {
            event.setCanceled(true);
        }
    }
}
