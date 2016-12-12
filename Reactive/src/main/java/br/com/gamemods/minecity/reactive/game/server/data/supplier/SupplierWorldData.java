package br.com.gamemods.minecity.reactive.game.server.data.supplier;

import br.com.gamemods.minecity.reactive.game.server.data.ServerData;
import br.com.gamemods.minecity.reactive.game.server.data.WorldData;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface SupplierWorldData extends SupplierServerData
{
    @NotNull
    WorldData getWorldData();

    @NotNull
    @Override
    default ServerData getServerData()
    {
        return getWorldData().getServerData();
    }
}
