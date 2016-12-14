package br.com.gamemods.minecity.forge.mc_1_10_2.listeners;

import br.com.gamemods.minecity.forge.base.MineCityForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

public class FrostTickListener
{
    private MineCityForge forge;

    public FrostTickListener(MineCityForge forge)
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
