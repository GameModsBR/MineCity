package br.com.gamemods.minecity.reactive.reactor.builtin;

import br.com.gamemods.minecity.reactive.reactor.EntityReactor;
import br.com.gamemods.minecity.reactive.reactor.ItemReactor;
import br.com.gamemods.minecity.reactive.reactor.Reactor;
import br.com.gamemods.minecity.reactive.reactor.ServerReactor;
import org.jetbrains.annotations.NotNull;

public class BuiltinReactor implements Reactor
{
    private BuiltinBlockReactor blockReactor = new BuiltinBlockReactor();

    @NotNull
    @Override
    public BuiltinBlockReactor getBlockReactor()
    {
        return blockReactor;
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
