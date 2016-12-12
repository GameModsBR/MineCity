package br.com.gamemods.minecity.reactive.reactor;

import org.jetbrains.annotations.NotNull;

public interface Reactor
{
    @NotNull
    BlockReactor getBlockReactor();

    @NotNull
    ItemReactor getItemReactor();

    @NotNull
    EntityReactor getEntityReactor();

    @NotNull
    ServerReactor getServerReactor();
}
