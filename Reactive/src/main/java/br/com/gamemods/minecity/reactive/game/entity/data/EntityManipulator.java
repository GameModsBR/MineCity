package br.com.gamemods.minecity.reactive.game.entity.data;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface EntityManipulator
{
    @NotNull
    Optional<EntityData> getEntityData(@NotNull Object entity);
}
