package br.com.gamemods.minecity.forge.base.protection;

import br.com.gamemods.minecity.MineCity;
import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.permission.Permissible;

import java.util.Optional;
import java.util.stream.Stream;

public interface Reaction
{
    Stream<Message> stream(MineCity mineCity, Permissible permissible);

    default Optional<Message> can(MineCity mineCity, Permissible permissible)
    {
        return stream(mineCity, permissible).findFirst();
    }
}
