package br.com.gamemods.minecity.forge.base.protection.reaction;

import br.com.gamemods.minecity.MineCity;
import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.permission.Permissible;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.stream.Stream;

public class DenyReaction implements Reaction
{
    @NotNull
    private Message message;

    public DenyReaction(@NotNull Message message)
    {
        this.message = message;
    }

    @Override
    public Stream<Message> stream(MineCity mineCity, Permissible permissible)
    {
        return Stream.of(message);
    }

    @Override
    public Optional<Message> can(MineCity mineCity, Permissible permissible)
    {
        return Optional.of(message);
    }

    @Override
    public boolean equals(Object o)
    {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;

        DenyReaction that = (DenyReaction) o;
        return message.equals(that.message);

    }

    @Override
    public int hashCode()
    {
        return message.hashCode();
    }
}
