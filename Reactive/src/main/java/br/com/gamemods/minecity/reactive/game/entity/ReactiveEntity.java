package br.com.gamemods.minecity.reactive.game.entity;

import br.com.gamemods.minecity.api.world.EntityPos;
import br.com.gamemods.minecity.api.world.MinecraftEntity;
import br.com.gamemods.minecity.reactive.reaction.Reaction;

public interface ReactiveEntity
{
    Reaction reactSpawn(MinecraftEntity entity, EntityPos pos);
}
