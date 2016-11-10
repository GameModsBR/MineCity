package br.com.gamemods.minecity.economy;

import br.com.gamemods.minecity.MineCity;

@FunctionalInterface
public interface EconomyProvider
{
    EconomyProxy createProxy(MineCity mineCity);
}
