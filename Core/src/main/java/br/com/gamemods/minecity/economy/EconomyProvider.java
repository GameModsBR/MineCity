package br.com.gamemods.minecity.economy;

import br.com.gamemods.minecity.MineCity;

import java.util.function.Function;

@FunctionalInterface
public interface EconomyProvider extends Function<MineCity, EconomyProxy>
{
    EconomyProxy createProxy(MineCity mineCity);

    @Override
    default EconomyProxy apply(MineCity mineCity)
    {
        return createProxy(mineCity);
    }
}
