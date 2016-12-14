package br.com.gamemods.minecity.forge.base.protection.opencomputers;

import br.com.gamemods.minecity.forge.base.accessors.block.ITileEntity;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.opencomputers.TileRobotProxyTransformer;

@Referenced(at = TileRobotProxyTransformer.class)
public interface IRobotProxyTile extends ITileEntity, IAgent
{
    @Referenced(at = TileRobotProxyTransformer.class)
    IRobotTile robotTile();
}
