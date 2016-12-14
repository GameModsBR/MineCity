package br.com.gamemods.minecity.forge.mc_1_10_2.accessors;

import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntity;
import br.com.gamemods.minecity.forge.base.accessors.world.IExplosion;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.mc_1_10_2.core.transformer.forge.FrostExplosionTransformer;
import br.com.gamemods.minecity.forge.mc_1_10_2.core.transformer.forge.FrostInterfaceTransformer;
import net.minecraft.world.Explosion;

@Referenced(at = FrostInterfaceTransformer.class)
public interface FrostExplosion extends IExplosion
{
    @Override
    default double getExplosionX()
    {
        return ((Explosion) this).getPosition().xCoord;
    }

    @Override
    default double getExplosionY()
    {
        return ((Explosion) this).getPosition().yCoord;
    }

    @Override
    default double getExplosionZ()
    {
        return ((Explosion) this).getPosition().zCoord;
    }

    @Referenced(at = FrostExplosionTransformer.class)
    @Override
    IEntity getExploder();
}
