package br.com.gamemods.minecity.forge.mc_1_7_10.event;

import cpw.mods.fml.common.eventhandler.Cancelable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraftforge.event.entity.EntityEvent;

@Cancelable
public class FishingHookHitEntityEvent extends EntityEvent
{
    public final EntityFishHook hook;

    public FishingHookHitEntityEvent(Entity entity, EntityFishHook hook)
    {
        super(entity);
        this.hook = hook;
    }
}
