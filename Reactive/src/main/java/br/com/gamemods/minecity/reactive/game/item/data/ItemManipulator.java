package br.com.gamemods.minecity.reactive.game.item.data;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;

public interface ItemManipulator
{
    @NotNull
    Optional<ItemData> getItemData(@NotNull Object item);

    @NotNull
    Optional<ItemStackData> getItemStackData(@NotNull Object stack);

    @NotNull
    Optional<ItemStateData> getItemStateData(@NotNull Object state);

    @NotNull
    Optional<ItemTraitData<?>> getItemTraitData(@NotNull Object trait);

    Collection<ItemData> findItemTypes(Class<?> clazz);
}
