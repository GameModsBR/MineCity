package br.com.gamemods.minecity.commands;

import br.com.gamemods.minecity.MineCity;
import br.com.gamemods.minecity.api.command.Command;
import br.com.gamemods.minecity.api.command.CommandResult;
import br.com.gamemods.minecity.api.command.CommandSender;
import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.datasource.api.DataSourceException;
import br.com.gamemods.minecity.structure.City;
import br.com.gamemods.minecity.structure.ClaimedChunk;
import br.com.gamemods.minecity.structure.Island;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

import static br.com.gamemods.minecity.api.StringUtil.identity;

public class CityCommand
{
    @NotNull
    private final MineCity mineCity;

    public CityCommand(@NotNull MineCity mineCity)
    {
        this.mineCity = mineCity;
    }

    @Command(value = "city.create", console = false)
    public CommandResult<City> create(CommandSender sender, String name) throws DataSourceException
    {
        String identity = identity(name);
        if(identity.length() <3)
            return new CommandResult<>(new Message("cmd.city.create.name.short",
                    "The name ${name} is not valid, try a bigger name",
                    new Object[]{"name",name}
            ));

        String conflict = mineCity.dataSource.checkNameConflict(name);
        if(conflict != null)
            return new CommandResult<>(new Message("cmd.city.create.name.conflict",
                    "The name ${name} conflicts with ${conflict}",
                    new Object[][]{{"name",name},{"conflict",conflict}})
            );

        BlockPos spawn = sender.getPosition();
        Optional<ClaimedChunk> optionalClaim = mineCity.getOrFetchChunk(spawn.getChunk());
        if(!optionalClaim.isPresent())
            return new CommandResult<>(new Message("cmd.city.create.chunk.not-loaded",
                    "The chunk that you are standing is not loaded properly"));

        ClaimedChunk claim = optionalClaim.get();
        Island island = claim.getIsland();
        if(island != null)
            return new CommandResult<>(new Message("cmd.city.create.chunk.claimed",
                    "The chunk that you are is already claimed to ${city}",
                    new Object[]{"city",island.getCity().getName()}
            ));

        City reserved = claim.getCity();
        if(reserved != null)
            return new CommandResult<>(new Message("cmd.city.create.chunk.reserved",
                    "The chunk that you are is reserved to ${city}", new Object[]{"city",reserved.getName()}
            ));

        City city = new City(mineCity, name, sender.getPlayerId(), spawn);
        return new CommandResult<>(new Message("cmd.city.create.chunk.success",
                "The city ${name} was created successfully, if you get lost you can teleport back with /city spawn ${identity}",
                new Object[][]{{"name", city.getName()},{"identity",city.getIdentityName()}}
        ), city);
    }
}
