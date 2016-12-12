package br.com.gamemods.minecity.reactive.game.entity.data;

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
}
