package br.com.gamemods.minecity.forge.base.protection;

import br.com.gamemods.minecity.MineCity;
import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.permission.Permissible;

import java.util.stream.Stream;

public class NoReaction implements Reaction
{
    public static final NoReaction INSTANCE = new NoReaction();

    private NoReaction()
    {}

    @Override
    public Stream<Message> stream(MineCity mineCity, Permissible permissible)
    {
        return Stream.empty();
    }
}
