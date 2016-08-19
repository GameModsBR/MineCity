package br.com.gamemods.minecity.bukkit.protection;

import br.com.gamemods.minecity.api.permission.FlagHolder;
import org.jetbrains.annotations.NotNull;

public class FlagHolderData
{
    public static final String KEY = "FlagHolderData";

    @NotNull
    public final FlagHolder home;

    public FlagHolderData(@NotNull FlagHolder home)
    {
        this.home = home;
    }
}
