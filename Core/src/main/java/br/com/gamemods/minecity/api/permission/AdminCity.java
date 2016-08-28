package br.com.gamemods.minecity.api.permission;

import br.com.gamemods.minecity.structure.City;
import org.jetbrains.annotations.NotNull;

public class AdminCity extends ServerAdmins<Integer>
{
    public AdminCity(City city)
    {
        super(city.getId(), ServerAdmins.INSTANCE.getName());
    }

    @NotNull
    @Override
    public String getName()
    {
        return ServerAdmins.INSTANCE.getName();
    }

    @Override
    public void setName(@NotNull String name)
    {
        ServerAdmins.INSTANCE.setName(name);
    }
}
