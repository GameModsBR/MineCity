package br.com.gamemods.minecity.reactive.reaction;

import br.com.gamemods.minecity.MineCity;
import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.permission.Permissible;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public class CombinedReaction implements Reaction
{
    @NotNull
    private Set<Reaction> combination;

    public CombinedReaction(Collection<Reaction> reactions)
    {
        this.combination = new LinkedHashSet<>(reactions);
    }

    public CombinedReaction(Reaction a, Reaction b)
    {
        combination = new LinkedHashSet<>(2);
        combination.add(a);
        combination.add(b);
    }

    @Override
    public Stream<Message> stream(MineCity mineCity, Permissible permissible)
    {
        return combination.stream().flatMap(reaction -> reaction.stream(mineCity, permissible));
    }

    @Override
    public Optional<Message> can(MineCity mineCity, Permissible permissible)
    {
        return stream(mineCity, permissible).findFirst();
    }
}
