package br.com.gamemods.minecity.forge.base.protection.industrialcraft;

import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntity;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityLivingBase;
import br.com.gamemods.minecity.forge.base.accessors.world.IExplosion;
import br.com.gamemods.minecity.forge.base.accessors.world.IWorldServer;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.industrialcraft.ExplosionIC2Transformer;

@Referenced(at = ExplosionIC2Transformer.class)
public interface IExplosionIC2 extends IExplosion
{
    @Referenced(at = ExplosionIC2Transformer.class)
    @Override
    double getExplosionX();

    @Referenced(at = ExplosionIC2Transformer.class)
    @Override
    double getExplosionY();

    @Referenced(at = ExplosionIC2Transformer.class)
    @Override
    double getExplosionZ();

    @Referenced(at = ExplosionIC2Transformer.class)
    @Override
    IEntity getExploder();

    @Referenced(at = ExplosionIC2Transformer.class)
    @Override
    IWorldServer getWorld();

    @Referenced(at = ExplosionIC2Transformer.class)
    IEntityLivingBase getIgniter();
}
