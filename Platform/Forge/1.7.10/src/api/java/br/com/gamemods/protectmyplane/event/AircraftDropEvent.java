package br.com.gamemods.protectmyplane.event;

import cpw.mods.fml.common.eventhandler.Cancelable;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraftforge.event.entity.EntityEvent;

import java.util.UUID;

@Cancelable
public class AircraftDropEvent extends EntityEvent
{
    public final Item item;
    public final int amount;
    public final float offset;
    public final UUID ownerId;
    public final String ownerName;

    public AircraftDropEvent(Entity entity, Item item, int amount, float offset, UUID ownerId, String ownerName)
    {
        super(entity);
        this.item = item;
        this.amount = amount;
        this.offset = offset;
        this.ownerId = ownerId;
        this.ownerName = ownerName;
    }
}