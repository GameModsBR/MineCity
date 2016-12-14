package br.com.gamemods.minecity.forge.mc_1_7_10.protection.opencomputers;

import br.com.gamemods.minecity.forge.base.ForgeUtil;
import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.protection.opencomputers.IAgent;
import br.com.gamemods.minecity.forge.base.protection.opencomputers.RobotProtections;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import li.cil.oc.api.event.RobotMoveEvent;

public class SevenRobotProtections extends RobotProtections
{
    public SevenRobotProtections(MineCityForge mod)
    {
        super(mod);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onRobotMove(RobotMoveEvent.Pre event)
    {
        if(onRobotMove(
                (IAgent) event.agent,
                ForgeUtil.toDirection(event.direction.ordinal())
        ))
        {
            event.setCanceled(true);
        }
    }
}
