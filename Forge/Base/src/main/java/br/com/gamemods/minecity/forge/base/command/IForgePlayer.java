package br.com.gamemods.minecity.forge.base.command;

import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.world.MinecraftEntity;
import br.com.gamemods.minecity.api.world.WorldDim;
import br.com.gamemods.minecity.structure.DisplayedSelection;
import org.jetbrains.annotations.NotNull;

public interface IForgePlayer extends MinecraftEntity
{
    void tick();

    @NotNull
    DisplayedSelection<?> getSelection(WorldDim world);

    PlayerID getPlayerId();
}
