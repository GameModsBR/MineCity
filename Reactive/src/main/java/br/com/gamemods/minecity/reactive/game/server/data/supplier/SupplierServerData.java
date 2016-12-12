package br.com.gamemods.minecity.reactive.game.server.data.supplier;

import br.com.gamemods.minecity.reactive.game.server.data.ServerData;
import org.jetbrains.annotations.NotNull;

public interface SupplierServerData
{
    @NotNull
    ServerData getServerData();
}
