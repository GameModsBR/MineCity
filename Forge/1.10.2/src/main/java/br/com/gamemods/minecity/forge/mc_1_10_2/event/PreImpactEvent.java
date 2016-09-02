package br.com.gamemods.minecity.forge.mc_1_10_2.event;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

@Cancelable
public class PreImpactEvent extends EntityEvent
{
    public final RayTraceResult traceResult;

    public PreImpactEvent(Entity entity, RayTraceResult traceResult)
    {
        super(entity);
        this.traceResult = traceResult;
    }
}
