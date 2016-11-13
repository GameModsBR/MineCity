package br.com.gamemods.minecity.permission;

import br.com.gamemods.minecity.MineCity;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class PermissionLayer
{
    private static Map<String, PermissionProvider> providers = new HashMap<>(2);
    static
    {
        register("none", mineCity -> new InternalPermission());
    }

    public static void register(@NotNull String name, @NotNull PermissionProvider provider)
    {
        name = name.toLowerCase();
        if(providers.putIfAbsent(Objects.requireNonNull(name, "name is null").toLowerCase(), Objects.requireNonNull(provider, "provider is null")) != null)
            throw new IllegalStateException("PermissionProvider "+name+" is already registered");
    }

    public static PermissionProxy load(@NotNull MineCity mineCity, @NotNull String name)
    {
        name = name.toLowerCase();
        PermissionProvider provider = providers.get(name);
        if(provider == null)
            throw new UnsupportedOperationException("PermissionProvider "+name+" is not supported on this server");

        return provider.createProxy(mineCity);
    }
}
