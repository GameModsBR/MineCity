package br.com.gamemods.minecity.reactive.reactor;

import br.com.gamemods.minecity.reactive.game.block.data.BlockManipulator;
import br.com.gamemods.minecity.reactive.game.entity.data.EntityManipulator;
import br.com.gamemods.minecity.reactive.game.item.data.ItemManipulator;
import br.com.gamemods.minecity.reactive.game.server.data.ServerManipulator;
import org.jetbrains.annotations.NotNull;

public interface Manipulator
{
    @NotNull
    BlockManipulator getBlockManipulator();

    @NotNull
    ItemManipulator getItemManipulator();

    @NotNull
    EntityManipulator getEntityManipulator();

    @NotNull
    ServerManipulator getServerManipulator();
}
