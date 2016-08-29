package br.com.gamemods.minecity.forge.mc_1_10_2.event;

import net.minecraft.entity.Entity;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import org.jetbrains.annotations.Nullable;

@Cancelable
public class EntityIgniteEvent extends EntityEvent
{
    @Nullable
    public final Object source;
    public final Class<?> sourceClass;
    public final String sourceMethod;
    public final String sourceMethodDesc;
    public final int ticks;

    public EntityIgniteEvent(Entity entity, int ticks, @Nullable Object source, Class<?> sourceClass, String sourceMethod, String sourceMethodDesc)
    {
        super(entity);
        this.source = source;
        this.sourceClass = sourceClass;
        this.sourceMethod = sourceMethod;
        this.sourceMethodDesc = sourceMethodDesc;
        this.ticks = ticks;
    }
}
