package br.com.gamemods.minecity.forge.mc_1_7_10.event;

import cpw.mods.fml.common.eventhandler.Cancelable;
import net.minecraft.entity.Entity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.event.entity.EntityEvent;

@Cancelable
public class PreImpactEvent extends EntityEvent
{
    public final MovingObjectPosition traceResult;

    public PreImpactEvent(Entity entity, MovingObjectPosition traceResult)
    {
        super(entity);
        this.traceResult = traceResult;
    }
}
