package br.com.gamemods.minecity.forge.base.protection.reaction;

import br.com.gamemods.minecity.MineCity;
import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.permission.Permissible;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class ObservedReaction implements Reaction
{
    private final List<Reaction> reactions;
    private List<Consumer<Reaction>> allowListeners;
    private List<BiConsumer<Reaction, Message>> denyListeners;

    public ObservedReaction(Reaction... reactions)
    {
        this(Arrays.asList(reactions));
    }

    public ObservedReaction(Collection<Reaction> reactions)
    {
        this.reactions = new ArrayList<>(reactions);
    }

    public ObservedReaction addAllowListener(Consumer<Reaction> consumer)
    {
        if(allowListeners == null)
            allowListeners = new ArrayList<>(1);
        allowListeners.add(consumer);
        return this;
    }

    public ObservedReaction addDenyListener(Consumer<Message> consumer)
    {
        return addDenyListener((reaction, message) -> consumer.accept(message));
    }

    public ObservedReaction addDenyListener(BiConsumer<Reaction, Message> consumer)
    {
        if(denyListeners == null)
            denyListeners = new ArrayList<>(1);
        denyListeners.add(consumer);
        return this;
    }

    protected void onAllow(Reaction reaction)
    {
        if(allowListeners == null)
            return;

        allowListeners.forEach(consumer -> consumer.accept(reaction));
    }

    protected void onDeny(Reaction reaction, Message message)
    {
        if(denyListeners == null)
            return;

        denyListeners.forEach(consumer -> consumer.accept(reaction, message));
    }

    @Override
    public Stream<Message> stream(MineCity mineCity, Permissible permissible)
    {
        return reactions.stream().map(reaction -> {
            Optional<Message> denial = reaction.can(mineCity, permissible);
            if(denial.isPresent())
            {
                Message message = denial.get();
                onDeny(reaction, message);
                return message;
            }
            else
            {
                onAllow(reaction);
                return null;
            }
        }).filter(message -> message != null);
    }
}
