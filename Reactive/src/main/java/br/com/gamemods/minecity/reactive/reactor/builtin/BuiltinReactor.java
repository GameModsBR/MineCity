package br.com.gamemods.minecity.reactive.reactor.builtin;

import br.com.gamemods.minecity.reactive.reactor.*;
import org.jetbrains.annotations.NotNull;

public class BuiltinReactor implements Reactor
{
    @NotNull
    @Override
    public BlockReactor getBlockReactor()
    {
        return null;
    }

    @NotNull
    @Override
    public ItemReactor getItemReactor()
    {
        return null;
    }

    @NotNull
    @Override
    public EntityReactor getEntityReactor()
    {
        return null;
    }

    @NotNull
    @Override
    public ServerReactor getServerReactor()
    {
        return null;
    }
}
