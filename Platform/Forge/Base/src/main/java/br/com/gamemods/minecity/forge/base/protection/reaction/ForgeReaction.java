package br.com.gamemods.minecity.forge.base.protection.reaction;

import br.com.gamemods.minecity.MineCity;
import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.permission.Permissible;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.reactive.reaction.Reaction;
import br.com.gamemods.minecity.reactive.reaction.ReactionListener;
import br.com.gamemods.minecity.reactive.reaction.TriggeredReaction;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.stream.Stream;

@Deprecated
public final class ForgeReaction extends TriggeredReaction implements ForgeTriggers
{
    @NotNull
    private final TriggeredReaction reaction;

    public ForgeReaction(@NotNull TriggeredReaction reaction)
    {
        this.reaction = reaction;
    }

    @NotNull
    @Override
    public TriggeredReaction self()
    {
        return reaction;
    }

    @Override
    public Stream<Message> stream(MineCity mineCity, Permissible permissible)
    {
        return reaction.stream(mineCity, permissible);
    }

    @Override
    public Optional<Message> can(MineCity mineCity, Permissible permissible)
    {
        return reaction.can(mineCity, permissible);
    }

    @Override
    public TriggeredReaction addDenialListener(ReactionListener listener)
    {
        return reaction.addDenialListener(listener);
    }

    @Override
    public Reaction combine(Reaction other)
    {
        return reaction.combine(other);
    }

    @Override
    public TriggeredReaction addAllowListener(ReactionListener listener)
    {
        return reaction.addAllowListener(listener);
    }

    public static Reaction combine(Stream<Reaction> reactions)
    {
        return Reaction.combine(reactions);
    }

    @Override
    protected void onDeny(Permissible permissible, PermissionFlag flag, BlockPos pos, Message message)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void onAllow(Permissible permissible, PermissionFlag flag, BlockPos pos)
    {
        throw new UnsupportedOperationException();
    }
}
