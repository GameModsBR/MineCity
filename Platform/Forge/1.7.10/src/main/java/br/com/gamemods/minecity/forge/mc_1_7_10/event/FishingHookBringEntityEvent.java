package br.com.gamemods.minecity.forge.mc_1_7_10.event;

import cpw.mods.fml.common.eventhandler.Cancelable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraftforge.event.entity.EntityEvent;

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
