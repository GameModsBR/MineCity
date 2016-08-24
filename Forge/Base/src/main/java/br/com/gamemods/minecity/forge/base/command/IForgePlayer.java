package br.com.gamemods.minecity.forge.base.command;

import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.world.MinecraftEntity;
import br.com.gamemods.minecity.api.world.WorldDim;
import br.com.gamemods.minecity.structure.DisplayedSelection;
import net.minecraft.block.Block;
import org.jetbrains.annotations.NotNull;

public interface IForgePlayer extends MinecraftEntity
{
    void tick();

    @NotNull
    DisplayedSelection<Block> getSelection(WorldDim world);

    PlayerID getPlayerId();
}
