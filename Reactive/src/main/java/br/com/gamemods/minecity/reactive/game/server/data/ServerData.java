package br.com.gamemods.minecity.reactive.game.server.data;

import br.com.gamemods.minecity.reactive.game.server.data.supplier.SupplierServerData;
import org.jetbrains.annotations.NotNull;

/**
 * Information about the server
 */
public interface ServerData extends SupplierServerData
{
    Object getServer();

    /**
     * Returns itself
     */
    @NotNull
    @Override
    default ServerData getServerData()
    {
        return this;
    }
}
