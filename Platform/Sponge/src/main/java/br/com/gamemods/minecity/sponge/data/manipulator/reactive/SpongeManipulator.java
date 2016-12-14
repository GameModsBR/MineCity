package br.com.gamemods.minecity.sponge.data.manipulator.reactive;

import br.com.gamemods.minecity.reactive.reactor.Manipulator;
import br.com.gamemods.minecity.sponge.MineCitySponge;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public class SpongeManipulator implements Manipulator
{
    public final MineCitySponge sponge;
    public final SpongeEntityManipulator entity;
    public final SpongeServerManipulator server;
    public final SpongeBlockManipulator block;
    public final SpongeItemManipulator item;

    public SpongeManipulator(MineCitySponge sponge)
    {
        this.sponge = sponge;

        entity = new SpongeEntityManipulator(this);
        server = new SpongeServerManipulator(this);
        block = new SpongeBlockManipulator(this);
        item = new SpongeItemManipulator(this);
    }

    static <Sup, Raw, Data> Data handleSupplier(ThreadLocal<Raw> threadLocal, Raw object, Class<Sup> tClass,
                                                 Function<Sup, Data> getter, Supplier<Data> creator)
    {
        if(threadLocal.get() == object || !tClass.isInstance(object))
            return creator.get();

        Sup casted = tClass.cast(object);
        try
        {
            threadLocal.set(object);
            return Objects.requireNonNull(getter.apply(casted));
        }
        finally
        {
            threadLocal.set(null);
        }
    }

    @NotNull
    @Override
    public SpongeBlockManipulator getBlockManipulator()
    {
        return block;
    }

    @NotNull
    @Override
    public SpongeItemManipulator getItemManipulator()
    {
        return item;
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
