package br.com.gamemods.minecity.reactive.reactor;

import br.com.gamemods.minecity.reactive.game.entity.ReactiveEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Created by joserobjr on 11/12/16.
 */
public interface EntityReactor
{
    @NotNull
    Optional<ReactiveEntity> getEntity(Object entity);
}
