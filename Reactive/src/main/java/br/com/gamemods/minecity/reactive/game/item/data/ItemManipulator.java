package br.com.gamemods.minecity.reactive.game.item.data;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface ItemManipulator
{
    @NotNull
    Optional<ItemData> getItemData(@NotNull Object item);
}
