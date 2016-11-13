package br.com.gamemods.minecity.economy;

import br.com.gamemods.minecity.MineCity;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class EconomyLayer
{
    private static Map<String, EconomyProvider> providers = new HashMap<>(2);
    static
    {
        register("none", mineCity -> new VoidEconomy());
    }

    public static void register(@NotNull String name, @NotNull EconomyProvider provider)
    {
        name = name.toLowerCase();
        if(providers.putIfAbsent(Objects.requireNonNull(name, "name is null").toLowerCase(), Objects.requireNonNull(provider, "provider is null")) != null)
            throw new IllegalStateException("EconomyProvider "+name+" is already registered");
    }

    public static EconomyProxy load(@NotNull MineCity mineCity, @NotNull String name)
    {
        name = name.toLowerCase();
        EconomyProvider provider = providers.get(name);
        if(provider == null)
            throw new UnsupportedOperationException("EconomyProvider "+name+" is not supported on this server");

        return provider.createProxy(mineCity);
    }
}
