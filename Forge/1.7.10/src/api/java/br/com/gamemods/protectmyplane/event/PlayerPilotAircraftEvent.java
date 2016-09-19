package br.com.gamemods.protectmyplane.event;

import cpw.mods.fml.common.eventhandler.Cancelable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;

import java.util.UUID;

@Cancelable
public class PlayerPilotAircraftEvent extends PlayerEvent
{
    public final Entity aircraft;
    public final UUID ownerId;
    public final String ownerName;

    public PlayerPilotAircraftEvent(EntityPlayer player, Entity aircraft, UUID ownerId, String ownerName)
    {
        super(player);
        this.aircraft = aircraft;
        this.ownerId = ownerId;
        this.ownerName = ownerName;
    }
}