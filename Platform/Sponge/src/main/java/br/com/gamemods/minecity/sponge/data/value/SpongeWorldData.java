package br.com.gamemods.minecity.sponge.data.value;

import br.com.gamemods.minecity.reactive.game.server.data.ServerData;
import br.com.gamemods.minecity.reactive.game.server.data.WorldData;
import br.com.gamemods.minecity.sponge.data.manipulator.reactive.SpongeManipulator;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.world.World;

public class SpongeWorldData implements WorldData
{
    private final SpongeManipulator manipulator;
    private final World world;

    public SpongeWorldData(SpongeManipulator manipulator, World world)
    {
        this.manipulator = manipulator;
        this.world = world;
    }

    @Override
    public World getWorld()
    {
        return world;
    }

    @NotNull
    @Override
    public ServerData getServerData()
    {
        return manipulator.server.getServerData(Sponge.getServer());
    }

    @Override
    public String toString()
    {
        return "SpongeWorldData{"+
                "world="+world+
                '}';
    }
}
