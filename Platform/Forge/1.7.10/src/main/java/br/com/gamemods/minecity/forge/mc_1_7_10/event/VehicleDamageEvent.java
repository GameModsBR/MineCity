package br.com.gamemods.minecity.forge.mc_1_7_10.event;

import cpw.mods.fml.common.eventhandler.Cancelable;
import net.minecraft.entity.Entity;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.EntityEvent;

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
