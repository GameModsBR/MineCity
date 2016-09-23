package br.com.gamemods.minecity.forge.base.protection.reaction;

import br.com.gamemods.minecity.MineCity;
import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.permission.Permissible;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

public interface Reaction
{
    Stream<Message> stream(MineCity mineCity, Permissible permissible);

    default Optional<Message> can(MineCity mineCity, Permissible permissible)
    {
        return stream(mineCity, permissible).findFirst();
    }

    default Reaction combine(Reaction other)
    {
        if(other instanceof NoReaction || other.equals(this))
            return this;

        return new CombinedReaction(this, other);
    }

    static Reaction combine(Stream<Reaction> reactions)
    {
        AtomicReference<Reaction> reaction = new AtomicReference<>(NoReaction.INSTANCE);
        reactions.forEachOrdered(r-> reaction.set(reaction.get().combine(r)));
        return reaction.get();
    }
}
