package br.com.gamemods.minecity.forge.mc_1_10_2.event;

import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import org.jetbrains.annotations.Nullable;

@Cancelable
public class EntityIgniteEntityEvent extends EntityIgniteEvent
{
    private final Entity igniter;

    public EntityIgniteEntityEvent(Entity entity, Entity igniter, @Nullable Object source, Class<?> sourceClass, String sourceMethod, String sourceMethodDesc)
    {
        super(entity, source, sourceClass, sourceMethod, sourceMethodDesc);
        this.igniter = igniter;
    }
}
