package br.com.gamemods.minecity.permission;

import br.com.gamemods.minecity.MineCity;

import java.util.function.Function;

@FunctionalInterface
public interface PermissionProvider extends Function<MineCity, PermissionProxy>
{
    PermissionProxy createProxy(MineCity mineCity);

    @Override
    default PermissionProxy apply(MineCity mineCity)
    {
        return createProxy(mineCity);
    }
}
