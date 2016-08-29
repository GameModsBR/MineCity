package br.com.gamemods.minecity.forge.mc_1_7_10.event;

import cpw.mods.fml.common.eventhandler.Cancelable;
import net.minecraft.entity.Entity;
import net.minecraftforge.event.entity.EntityEvent;
import org.jetbrains.annotations.Nullable;

@Cancelable
public class EntityIgniteEvent extends EntityEvent
{
    @Nullable
    public final Object source;
    public final Class<?> sourceClass;
    public final String sourceMethod;
    public final String sourceMethodDesc;

    public EntityIgniteEvent(Entity entity, @Nullable Object source, Class<?> sourceClass, String sourceMethod, String sourceMethodDesc)
    {
        super(entity);
        this.source = source;
        this.sourceClass = sourceClass;
        this.sourceMethod = sourceMethod;
        this.sourceMethodDesc = sourceMethodDesc;
    }
}
