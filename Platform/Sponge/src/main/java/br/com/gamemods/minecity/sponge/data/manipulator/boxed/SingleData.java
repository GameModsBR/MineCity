package br.com.gamemods.minecity.sponge.data.manipulator.boxed;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.immutable.common.AbstractImmutableSingleData;
import org.spongepowered.api.data.manipulator.mutable.common.AbstractSingleData;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;

import java.util.Optional;

public abstract class SingleData
        <D, M extends SingleData<D, M, U>, U extends SingleData.Immutable<D, M, U>>
    extends AbstractSingleData<D, M, U>
{
    protected final Key<Value<D>> key;

    public SingleData(D value, Key<Value<D>> key)
    {
        super(value, key);
        this.key = key;
    }

    @NotNull
    @Override
    protected Value<?> getValueGetter()
    {
        return Sponge.getRegistry().getValueFactory().createValue(key, getValue());
    }

    @Override
    public Optional<M> fill(DataHolder dataHolder, MergeFunction overlap)
    {
        M warpData = Preconditions.checkNotNull(overlap).merge(copy(), from(dataHolder.toContainer()).orElse(null));
        return Optional.of(set(key, warpData.get(key).get()));
    }

    @Override
    public Optional<M> from(DataContainer container)
    {
        if(container.contains(key.getQuery()))
            return Optional.of(set(key, (D) container.get(key.getQuery()).orElse(null)));

        return Optional.empty();
    }

    @Override
    public int getContentVersion()
    {
        return 1;
    }

    public abstract static class Immutable
            <D, M extends SingleData<D, M, U>, U extends Immutable<D, M, U>>
        extends AbstractImmutableSingleData<D, U, M>
    {
        protected final Key<Value<D>> key;

        public Immutable(D value, Key<Value<D>> key)
        {
            super(value, key);
            this.key = key;
        }

        @Override
        protected ImmutableValue<?> getValueGetter()
        {
            return Sponge.getRegistry().getValueFactory().createValue(key, getValue()).asImmutable();
        }

        @Override
        public int getContentVersion()
        {
            return 1;
        }

        @Override
        public String toString()
        {
            return "Immutable{"+
                    "key="+key+
                    '}';
        }
    }

    @Override
    public String toString()
    {
        return "SingleData{"+
                "key="+key+
                '}';
    }
}
