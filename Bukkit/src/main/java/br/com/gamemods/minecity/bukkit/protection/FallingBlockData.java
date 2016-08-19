package br.com.gamemods.minecity.bukkit.protection;

import br.com.gamemods.minecity.api.permission.FlagHolder;
import org.jetbrains.annotations.NotNull;

public class FallingBlockData
{
    public static final String KEY = "Falling";

    @NotNull
    public final FlagHolder home;

    public FallingBlockData(@NotNull FlagHolder home)
    {
        this.home = home;
    }
}
