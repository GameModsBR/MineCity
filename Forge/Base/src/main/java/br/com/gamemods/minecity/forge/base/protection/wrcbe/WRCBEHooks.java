package br.com.gamemods.minecity.forge.base.protection.wrcbe;

import br.com.gamemods.minecity.forge.base.accessors.block.ITileEntity;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntity;
import br.com.gamemods.minecity.forge.base.accessors.entity.projectile.ProjectileShooter;
import br.com.gamemods.minecity.forge.base.core.ModEnv;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.wrcbecore.JammerPartTransformer;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.wrcbecore.WirelessBoltTransformer;
import br.com.gamemods.minecity.forge.base.protection.ShooterDamageSource;
import net.minecraft.entity.Entity;
import net.minecraft.util.DamageSource;

@Referenced
public class WRCBEHooks
{
    @Referenced(at = WirelessBoltTransformer.class)
    public static boolean onBoltJamTile(Object tile, Object bolt)
    {
        return true;
    }

    @Referenced(at = WirelessBoltTransformer.class)
    public static boolean onBoltJamEntity(IWirelessBolt bolt, IEntity entity)
    {
        ProjectileShooter shooter = bolt.getShooter();
        if(shooter == null)
            return true;

        ShooterDamageSource custom = new ShooterDamageSource("bolt", shooter);
        return ModEnv.entityProtections.onEntityDamage(entity, custom, 1);
    }

    @Referenced(at = WirelessBoltTransformer.class)
    public static boolean onBoltAttackEntity(Entity entity, DamageSource source, float damage, IWirelessBolt bolt)
    {
        ProjectileShooter shooter = bolt.getShooter();
        if(shooter == null)
            return false;

        ShooterDamageSource custom = new ShooterDamageSource(source.damageType, shooter);
        return !ModEnv.entityProtections.onEntityDamage((IEntity) entity, custom, damage) &&
                entity.attackEntityFrom(source, damage);

    }

    @Referenced(at = JammerPartTransformer.class)
    public static boolean onJammerJamTile(IJammerPart part, ITileEntity tile)
    {
        return true;
    }

    @Referenced(at = JammerPartTransformer.class)
    public static boolean onJammerJamEntity(IJammerPart part, IEntity entity)
    {
        ShooterDamageSource custom = new ShooterDamageSource("bolt", new ProjectileShooter(part.tileI().getBlockPos(ModEnv.entityProtections.mod).toEntity()));
        return ModEnv.entityProtections.onEntityDamage(entity, custom, 1);
    }
}
