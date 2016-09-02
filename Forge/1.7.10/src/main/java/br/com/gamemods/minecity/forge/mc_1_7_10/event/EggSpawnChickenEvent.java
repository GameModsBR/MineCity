package br.com.gamemods.minecity.forge.mc_1_7_10.event;

import cpw.mods.fml.common.eventhandler.Cancelable;
import net.minecraft.entity.projectile.EntityEgg;
import net.minecraftforge.event.entity.EntityEvent;

@Cancelable
public class EggSpawnChickenEvent extends EntityEvent
{
    public EggSpawnChickenEvent(EntityEgg entity)
    {
        super(entity);
    }
}
