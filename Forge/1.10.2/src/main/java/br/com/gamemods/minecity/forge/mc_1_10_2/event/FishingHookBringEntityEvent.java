package br.com.gamemods.minecity.forge.mc_1_10_2.event;

import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

@Cancelable
public class FishingHookBringEntityEvent extends EntityEvent
{
    public final EntityFishHook hook;

    public FishingHookBringEntityEvent(Entity entity, EntityFishHook hook)
    {
        super(entity);
        this.hook = hook;
    }
}
