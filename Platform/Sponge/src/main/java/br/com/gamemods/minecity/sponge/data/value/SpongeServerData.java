package br.com.gamemods.minecity.sponge.data.value;

import br.com.gamemods.minecity.reactive.game.server.data.ServerData;
import br.com.gamemods.minecity.sponge.data.manipulator.reactive.SpongeManipulator;
import org.spongepowered.api.Server;

public class SpongeServerData implements ServerData
{
    private final SpongeManipulator manipulator;
    public final Server server;

    public SpongeServerData(SpongeManipulator manipulator, Server server)
    {
        this.manipulator = manipulator;
        this.server = server;
    }

    @Override
    public Server getServer()
    {
        return server;
    }
}
