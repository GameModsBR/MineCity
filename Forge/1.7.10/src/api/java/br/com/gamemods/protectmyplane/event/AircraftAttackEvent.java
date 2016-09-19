package br.com.gamemods.protectmyplane.event;

import cpw.mods.fml.common.eventhandler.Cancelable;
import net.minecraft.entity.Entity;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.EntityEvent;

import java.util.UUID;

@Cancelable
public class AircraftAttackEvent extends EntityEvent
{
    public final DamageSource source;
    public final float amount;
    public final UUID ownerId;
    public final String ownerName;

    public AircraftAttackEvent(Entity aircraft, DamageSource source, float amount, UUID ownerId, String ownerName)
    {
        super(aircraft);
        this.source = source;
        this.amount = amount;
        this.ownerId = ownerId;
        this.ownerName = ownerName;
    }
}