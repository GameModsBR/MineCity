package br.com.gamemods.minecity.forge.mc_1_10_2.event;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

import java.util.Collections;
import java.util.List;

@Cancelable
public class PostImpactEvent extends EntityEvent
{
    public final RayTraceResult traceResult;
    public final List<BlockSnapshot> changes;

    public PostImpactEvent(Entity entity, RayTraceResult traceResult, List<BlockSnapshot> changes)
    {
        super(entity);
        this.traceResult = traceResult;
        this.changes = Collections.unmodifiableList(changes);
    }
}
