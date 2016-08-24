package br.com.gamemods.minecity.protection;

import br.com.gamemods.minecity.api.Server;
import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.world.WorldDim;
import br.com.gamemods.minecity.structure.City;
import br.com.gamemods.minecity.structure.Nature;
import br.com.gamemods.minecity.structure.Plot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public interface MovementListener<Entity, S extends Server>
{
    Optional<Message> onCityChange(@NotNull City city, @Nullable Plot plot);
    Optional<Message> onPlotEnter(@NotNull Plot plot);
    Optional<Message> onPlotLeave(@NotNull City city);
    Optional<Message> onCityLeave(@NotNull Nature nature);
    Optional<Message> onNatureChange(@NotNull Nature nature);

    boolean isSafeToStep(S server, Entity entity, WorldDim world, int x, int y, int z);
}
