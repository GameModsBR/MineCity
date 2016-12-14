package br.com.gamemods.minecity.forge.mc_1_10_2.event;

import net.minecraft.entity.Entity;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

@Cancelable
public class VehicleDamageEvent extends EntityEvent
{
    public final DamageSource source;
    public final float amount;
    public VehicleDamageEvent(Entity entity, DamageSource source, float amount)
    {
        super(entity);
        this.source = source;
        this.amount = amount;
    }
}
