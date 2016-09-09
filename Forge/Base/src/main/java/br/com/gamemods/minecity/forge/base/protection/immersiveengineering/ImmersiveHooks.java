package br.com.gamemods.minecity.forge.base.protection.immersiveengineering;

import br.com.gamemods.minecity.forge.base.accessors.item.IItemSeeds;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.immersiveengineering.EntityChemthrowerShotTransformer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;

import java.lang.reflect.Field;

public class ImmersiveHooks
{
    private static Field itemSeeds;
    private static Field itemMaterial;
    private static Class<?> classIEContent;

    @Referenced(at = EntityChemthrowerShotTransformer.class)
    public static boolean onChemthrowerDamage(Entity entity, DamageSource source, float damage, EntityArrow shot)
    {
        EntityDamageSourceIndirect indirect = new EntityDamageSourceIndirect(source.damageType, shot, shot.shootingEntity == null? shot : shot.shootingEntity);
        return entity.attackEntityFrom(indirect.setFireDamage().setProjectile(), damage);
    }

    private static Class<?> getIEContent() throws ReflectiveOperationException
    {
        if(classIEContent == null)
            classIEContent = Class.forName("blusunrize.immersiveengineering.common.IEContent");
        return classIEContent;
    }

    public static IItemSeeds getItemSeeds()
    {
        try
        {
            if(itemSeeds == null)
                itemSeeds = getIEContent().getDeclaredField("itemSeeds");
            return (IItemSeeds) itemSeeds.get(null);
        }
        catch(ReflectiveOperationException e)
        {
            throw new UnsupportedOperationException(e);
        }
    }

    public static IItemSeeds getItemMaterial()
    {
        try
        {
            if(itemMaterial == null)
                itemMaterial = getIEContent().getDeclaredField("itemMaterial");
            return (IItemSeeds) itemMaterial.get(null);
        }
        catch(ReflectiveOperationException e)
        {
            throw new UnsupportedOperationException(e);
        }
    }
}
