package br.com.gamemods.minecity.forge.mc_1_10_2.event;

import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

@Cancelable
public class XpOrbTargetPlayerEvent extends PlayerEvent
{
    public final EntityXPOrb orb;

    public XpOrbTargetPlayerEvent(EntityPlayer player, EntityXPOrb orb)
    {
        super(player);
        this.orb = orb;
    }
}
