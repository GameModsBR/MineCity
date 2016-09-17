package br.com.gamemods.minecity.forge.base.protection.opencomputers;

import br.com.gamemods.minecity.forge.base.accessors.block.ITileEntity;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.ModInterfacesTransformer;
import scala.Option;

@Referenced(at= ModInterfacesTransformer.class)
public interface IRobotTile extends ITileEntity, IAgent, IEnvironmentHost
{
    Option moveFrom();
}
