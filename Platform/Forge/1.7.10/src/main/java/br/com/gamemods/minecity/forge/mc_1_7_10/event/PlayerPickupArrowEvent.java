package br.com.gamemods.minecity.forge.mc_1_7_10.event;

import cpw.mods.fml.common.eventhandler.Cancelable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraftforge.event.entity.player.PlayerEvent;

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
