package br.com.gamemods.minecity.forge.mc_1_7_10.event;

import cpw.mods.fml.common.eventhandler.Cancelable;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;

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
