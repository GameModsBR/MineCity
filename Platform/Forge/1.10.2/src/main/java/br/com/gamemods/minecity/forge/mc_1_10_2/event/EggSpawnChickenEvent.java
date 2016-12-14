package br.com.gamemods.minecity.forge.mc_1_10_2.event;

import net.minecraft.entity.projectile.EntityEgg;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

@Cancelable
public class EggSpawnChickenEvent extends EntityEvent
{
    public EggSpawnChickenEvent(EntityEgg entity)
    {
        super(entity);
    }
}
