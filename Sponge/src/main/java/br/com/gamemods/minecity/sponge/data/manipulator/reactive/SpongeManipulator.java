package br.com.gamemods.minecity.sponge.data.manipulator.reactive;

import br.com.gamemods.minecity.reactive.game.item.data.ItemManipulator;
import br.com.gamemods.minecity.reactive.reactor.Manipulator;
import br.com.gamemods.minecity.sponge.MineCitySponge;
import org.jetbrains.annotations.NotNull;

public class SpongeManipulator implements Manipulator
{
    public final MineCitySponge sponge;
    public final SpongeEntityManipulator entity;
    public final SpongeServerManipulator server;
    public final SpongeBlockManipulator block;

    public SpongeManipulator(MineCitySponge sponge)
    {
        this.sponge = sponge;

        entity = new SpongeEntityManipulator(this);
        server = new SpongeServerManipulator(this);
        block = new SpongeBlockManipulator(this);
    }

    @NotNull
    @Override
    public SpongeBlockManipulator getBlockManipulator()
    {
        return block;
    }

    @NotNull
    @Override
    public ItemManipulator getItemManipulator()
    {
        throw new UnsupportedOperationException("not implemented");
    }

    @NotNull
    @Override
    public SpongeEntityManipulator getEntityManipulator()
    {
        return entity;
    }

    @NotNull
    @Override
    public SpongeServerManipulator getServerManipulator()
    {
        return server;
    }
}
