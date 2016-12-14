package br.com.gamemods.minecity.reactive.game.item.data.supplier;

import br.com.gamemods.minecity.reactive.game.item.data.ItemStateData;
import org.jetbrains.annotations.NotNull;

public interface SupplierItemStateData
{
    @NotNull
    ItemStateData getItemStateData();
}
