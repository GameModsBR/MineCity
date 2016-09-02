package br.com.gamemods.minecity.forge.mc_1_7_10.accessors.entity;

import br.com.gamemods.minecity.forge.base.accessors.entity.base.IPotionEffect;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.mc_1_7_10.core.transformer.forge.SevenInterfaceTransformer;
import net.minecraft.potion.Potion;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

@Referenced(at = SevenInterfaceTransformer.class)
public interface SevenPotionEffect extends IPotionEffect
{
    @Override
    default boolean isNegative()
    {
        Potion potion = Potion.potionTypes[getForgeObject().getPotionID()];
        for(Field field : Potion.class.getDeclaredFields())
        {
            if(field.getType() != Boolean.TYPE)
                continue;

            int mod = field.getModifiers();
            if(Modifier.isFinal(mod) && Modifier.isPrivate(mod))
            {
                field.setAccessible(true);
                try
                {
                    return (Boolean) field.get(potion);
                }
                catch(IllegalAccessException e)
                {
                    e.printStackTrace();
                    return true;
                }
            }
        }

        return true;
    }
}
