package br.com.gamemods.minecity.forge.base.protection.wrcbe;

import br.com.gamemods.minecity.forge.base.accessors.entity.projectile.Projectile;
import br.com.gamemods.minecity.forge.base.accessors.entity.projectile.ProjectileShooter;
import br.com.gamemods.minecity.forge.base.core.ModEnv;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.wrcbecore.JammerPartTransformer;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.wrcbecore.WirelessBoltTransformer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Referenced(at = WirelessBoltTransformer.class)
public interface IWirelessBolt extends Projectile
{
    @Referenced(at = JammerPartTransformer.class)
    default IWirelessBolt createdFromPart(IJammerPart part)
    {
        setShooter(new ProjectileShooter(part.tileI().getBlockPos(ModEnv.entityProtections.mod).toEntity()));
        return this;
    }

    @SuppressWarnings("deprecation")
    @Nullable
    @Override
    default ProjectileShooter getShooter()
    {
        return getMineCityShooter();
    }

    @SuppressWarnings("deprecation")
    @Override
    default void setShooter(@NotNull ProjectileShooter shooter)
    {
        setMineCityShooter(shooter);
    }
}
