package br.com.gamemods.minecity.api.permission;

import br.com.gamemods.minecity.api.PlayerID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class OptionalPlayer<T> extends Identity<T>
{
    public OptionalPlayer(@NotNull T id, @NotNull String name)
    {
        super(id, name);
    }

    @Nullable
    public final PlayerID player()
    {
        if(this instanceof PlayerID)
            return (PlayerID) this;

        return null;
    }
}
