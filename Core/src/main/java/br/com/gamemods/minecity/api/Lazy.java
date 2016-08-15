package br.com.gamemods.minecity.api;

import java.util.function.Supplier;

public class Lazy<T> implements Supplier<T>
{
    protected Supplier<T> supplier;
    protected T obj;

    public Lazy(Supplier<T> supplier)
    {
        this.supplier = supplier;
    }

    @Override
    public T get()
    {
        if(obj != null)
            return obj;

        return obj = supplier.get();
    }
}
