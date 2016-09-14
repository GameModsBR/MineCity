package br.com.gamemods.minecity.forge.base.protection.reaction;

import br.com.gamemods.minecity.api.permission.Permissible;
import org.jetbrains.annotations.NotNull;

public abstract class BlameOtherReaction implements Reaction
{
    @NotNull
    protected Permissible other;

    public BlameOtherReaction(@NotNull Permissible other)
    {
        this.other = other;
    }
}
