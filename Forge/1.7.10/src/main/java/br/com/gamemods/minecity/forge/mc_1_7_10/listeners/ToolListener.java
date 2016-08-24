package br.com.gamemods.minecity.forge.mc_1_7_10.listeners;

import br.com.gamemods.minecity.forge.base.MineCityForge;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

public class ToolListener extends br.com.gamemods.minecity.forge.base.listeners.ToolListener
{
    public ToolListener(MineCityForge forge)
    {
        super(forge);
    }

    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        if(event.world.isRemote || event.action == PlayerInteractEvent.Action.RIGHT_CLICK_AIR)
            return;

        if(onPlayerInteract(
                event.entityPlayer, event.entityPlayer.getHeldItem(),
                event.world, event.x, event.y, event.z,
                event.action == PlayerInteractEvent.Action.LEFT_CLICK_BLOCK
        ))
        {
            event.setCanceled(true);
        }
    }
}
