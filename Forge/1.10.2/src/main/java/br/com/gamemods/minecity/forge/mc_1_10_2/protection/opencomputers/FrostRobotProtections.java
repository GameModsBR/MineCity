package br.com.gamemods.minecity.forge.mc_1_10_2.protection.opencomputers;

import br.com.gamemods.minecity.forge.base.protection.opencomputers.IAgent;
import br.com.gamemods.minecity.forge.base.protection.opencomputers.RobotProtections;
import br.com.gamemods.minecity.forge.mc_1_10_2.FrostUtil;
import br.com.gamemods.minecity.forge.mc_1_10_2.MineCityFrost;
import li.cil.oc.api.event.RobotMoveEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class FrostRobotProtections extends RobotProtections
{
    private MineCityFrost mod;
    public FrostRobotProtections(MineCityFrost forge)
    {
        super(forge);
        mod = forge;
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onRobotMove(RobotMoveEvent.Pre event)
    {
        if(onRobotMove(
                (IAgent) event.agent,
                FrostUtil.toDirection(event.direction)
        ))
        {
            event.setCanceled(true);
        }
    }

    /* Done by fake entity before this event is fired
    @SubscribeEvent(priority = EventPriority.LOW)
    public void onRobotPlace(RobotPlaceBlockEvent event)
    {
        if(onRobotPlaceBlock(
                (IAgent) event.agent,
                (IItemStack) (Object) event.stack,
                mod.block(event.world, event.pos)
        ))
        {
            event.setCanceled(true);
        }
    } */
}
