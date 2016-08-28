package br.com.gamemods.minecity.api.permission;

import br.com.gamemods.minecity.api.world.WorldDim;
import org.jetbrains.annotations.NotNull;

public class NatureID extends Identity<WorldDim>
{
    private static final long serialVersionUID = -6074055085321354433L;

    public NatureID(@NotNull WorldDim world)
    {
        super(world, world.name());
    }

    @NotNull
    @Override
    public String getName()
    {
        return uniqueId.name();
    }

    @Override
    public void setName(@NotNull String name)
    {
        uniqueId.name = name;
    }

    @Override
    public Type getType()
    {
        return Type.NATURE;
    }

    @Override
    public String toString()
    {
        return "NatureID{" +
                "world=" + uniqueId +
                "} ";
    }
}
