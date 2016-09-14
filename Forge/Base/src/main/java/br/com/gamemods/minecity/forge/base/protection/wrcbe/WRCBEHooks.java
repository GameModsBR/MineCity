package br.com.gamemods.minecity.forge.base.protection.wrcbe;

import br.com.gamemods.minecity.api.permission.Identity;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.accessors.block.ITileEntity;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntity;
import br.com.gamemods.minecity.forge.base.accessors.entity.projectile.ProjectileShooter;
import br.com.gamemods.minecity.forge.base.core.ModEnv;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.wrcbecore.JammerPartTransformer;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.wrcbecore.WirelessBoltTransformer;
import br.com.gamemods.minecity.forge.base.protection.ShooterDamageSource;
import br.com.gamemods.minecity.structure.ClaimedChunk;
import net.minecraft.entity.Entity;
import net.minecraft.util.DamageSource;

@Referenced
public class WRCBEHooks
{
    @Referenced(at = WirelessBoltTransformer.class)
    public static boolean onBoltJamTile(ITileEntity tile, IWirelessBolt bolt)
    {
        ProjectileShooter shooter = bolt.getShooter();
        if(shooter == null)
            return true;

        MineCityForge mod = ModEnv.entityProtections.mod;
        BlockPos boltPos = shooter.getPos().getBlock();
        ClaimedChunk partClaim = mod.mineCity.provideChunk(boltPos.getChunk());
        Identity<?> owner = partClaim.getFlagHolder(boltPos).owner();

        BlockPos tilePos = new BlockPos(boltPos, tile.getPosX(), tile.getPosY(), tile.getPosZ());
        return mod.mineCity.provideChunk(tilePos.getChunk(), partClaim).getFlagHolder(tilePos).can(owner, PermissionFlag.MODIFY).isPresent();
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
        MineCityForge mod = ModEnv.entityProtections.mod;
        BlockPos partPos = part.tileI().getBlockPos(mod);
        ClaimedChunk partClaim = mod.mineCity.provideChunk(partPos.getChunk());
        Identity<?> owner = partClaim.getFlagHolder(partPos).owner();

        BlockPos tilePos = new BlockPos(partPos, tile.getPosX(), tile.getPosY(), tile.getPosZ());
        return mod.mineCity.provideChunk(tilePos.getChunk(), partClaim).getFlagHolder(tilePos).can(owner, PermissionFlag.MODIFY).isPresent();
    }

    @Referenced(at = JammerPartTransformer.class)
    public static boolean onJammerJamEntity(IJammerPart part, IEntity entity)
    {
        ShooterDamageSource custom = new ShooterDamageSource("bolt", new ProjectileShooter(part.tileI().getBlockPos(ModEnv.entityProtections.mod).toEntity()));
        return ModEnv.entityProtections.onEntityDamage(entity, custom, 1);
    }
}
