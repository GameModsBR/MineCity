package br.com.gamemods.minecity.forge.mc_1_7_10.event;

import cpw.mods.fml.common.eventhandler.Cancelable;
import net.minecraft.entity.Entity;
import org.jetbrains.annotations.Nullable;

@Cancelable
public class EntityIgniteEntityEvent extends EntityIgniteEvent
{
    public final Entity igniter;

    public EntityIgniteEntityEvent(Entity entity, Entity igniter, int ticks, @Nullable Object source, Class<?> sourceClass, String sourceMethod, String sourceMethodDesc)
    {
        super(entity, ticks, source, sourceClass, sourceMethod, sourceMethodDesc);
        this.igniter = igniter;
    }
}
