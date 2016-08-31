package br.com.gamemods.minecity.forge.mc_1_10_2.event;

import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraftforge.event.entity.EntityEvent;

public class EntitySpawnByFishingHookEvent extends EntityEvent
{
    public final EntityFishHook hook;
    public EntitySpawnByFishingHookEvent(Entity entity, EntityFishHook hook)
    {
        super(entity);
        this.hook = hook;
    }
}
