package br.com.gamemods.minecity.reactive.game.item;

import br.com.gamemods.minecity.reactive.game.item.data.ItemData;
import br.com.gamemods.minecity.reactive.game.item.data.ItemStateData;

public interface ReactiveItemStack
{
    ItemData getItemData();
    ItemStateData getItemStateData();
}
