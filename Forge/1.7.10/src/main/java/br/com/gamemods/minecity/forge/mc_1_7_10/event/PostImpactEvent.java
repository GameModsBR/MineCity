package br.com.gamemods.minecity.forge.mc_1_7_10.event;

import cpw.mods.fml.common.eventhandler.Cancelable;
import net.minecraft.entity.Entity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.entity.EntityEvent;

import java.util.Collections;
import java.util.List;

@Cancelable
public class PostImpactEvent extends EntityEvent
{
    public final MovingObjectPosition traceResult;
    public final List<BlockSnapshot> changes;

    public PostImpactEvent(Entity entity, MovingObjectPosition traceResult, List<BlockSnapshot> changes)
    {
        super(entity);
        this.traceResult = traceResult;
        this.changes = Collections.unmodifiableList(changes);
    }
}
