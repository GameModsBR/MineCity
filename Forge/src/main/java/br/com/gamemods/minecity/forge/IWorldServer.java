package br.com.gamemods.minecity.forge;

import br.com.gamemods.minecity.api.world.WorldDim;

public interface IWorldServer
{
    WorldDim getMineCityWorld();
    void setMineCityWorld(WorldDim world);
}
