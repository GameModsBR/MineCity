package br.com.gamemods.minecity.forge.base.command;

import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.world.MinecraftEntity;
import br.com.gamemods.minecity.api.world.WorldDim;
import br.com.gamemods.minecity.structure.DisplayedSelection;
import net.minecraft.block.Block;

public interface IForgePlayer extends MinecraftEntity
{
    void tick();

    DisplayedSelection<Block> getSelection(WorldDim world);

    PlayerID getPlayerId();
}
