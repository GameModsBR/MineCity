package br.com.gamemods.minecity.forge.mc_1_10_2.event;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

@Cancelable
public class PlayerPickupArrowEvent extends PlayerEvent
{
    public final EntityArrow arrow;

    public PlayerPickupArrowEvent(EntityPlayer player, EntityArrow arrow)
    {
        super(player);
        this.arrow = arrow;
    }
}
