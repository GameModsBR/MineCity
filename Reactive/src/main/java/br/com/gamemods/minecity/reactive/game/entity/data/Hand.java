package br.com.gamemods.minecity.reactive.game.entity.data;

import java.util.Arrays;

public enum Hand
{
    MAIN, OFF;
    private transient Object instance;

    public Object getInstance()
    {
        return instance;
    }

    public void setInstance(Object instance)
    {
        this.instance = instance;
    }

    public static Hand from(Object instance)
    {
        return Arrays.stream(values()).filter(it-> instance.equals(it.instance)).findFirst().get();
    }
}
