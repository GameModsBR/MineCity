package br.com.gamemods.minecity.economy;

import br.com.gamemods.minecity.MineCity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class DelayedProxy<T>
{
    @NotNull
    private MineCity mineCity;

    @NotNull
    private Function<MineCity, T> provider;

    @Nullable
    private T proxy;

    public DelayedProxy(@NotNull MineCity mineCity, @NotNull Function<MineCity, T> provider)
    {
        this.mineCity = mineCity;
        this.provider = provider;
    }

    public T provide()
    {
        T proxy = this.proxy;
        if(proxy != null)
            return proxy;

        synchronized(this)
        {
            proxy = this.proxy;
            if(proxy != null)
                return proxy;

            System.out.println("Loading the delayed proxy: "+provider);
            this.proxy = proxy = provider.apply(mineCity);
            return proxy;
        }
    }
}
