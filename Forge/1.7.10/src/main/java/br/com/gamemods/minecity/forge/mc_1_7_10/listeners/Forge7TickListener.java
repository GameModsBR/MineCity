package br.com.gamemods.minecity.forge.mc_1_7_10.listeners;

import br.com.gamemods.minecity.forge.base.MineCityForge;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;

public class Forge7TickListener
{
    private MineCityForge forge;

    public Forge7TickListener(MineCityForge forge)
    {
        this.forge = forge;
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        if(event.phase == TickEvent.Phase.END || event.side != Side.SERVER )
            return;

        forge.player(event.player).tick();
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event)
    {
        if(event.phase == TickEvent.Phase.END || event.side != Side.SERVER)
            return;

        forge.onServerTick();
    }
}
