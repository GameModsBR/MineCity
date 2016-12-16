package br.com.gamemods.minecity.sponge.data.manipulator.boxed;

import br.com.gamemods.minecity.reactive.game.block.data.TileEntityData;
import br.com.gamemods.minecity.reactive.game.entity.data.EntityData;
import com.google.common.reflect.TypeToken;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.KeyFactory;
import org.spongepowered.api.data.value.mutable.Value;

public class MineCityKeys
{
    public static final Key<Value<EntityData>> ENTITY_DATA = KeyFactory.makeSingleKey(
            TypeToken.of(EntityData.class),
            new TypeToken<Value<EntityData>>(){},
            DataQuery.of("MineCityEntity"), "minecity:entity", "Entity Data"
    );

    public static final Key<Value<TileEntityData>> TILE_ENTITY_DATA = KeyFactory.makeSingleKey(
            TypeToken.of(TileEntityData.class),
            new TypeToken<Value<TileEntityData>>(){},
            DataQuery.of("MineCityTE"), "minecity:tile_entity", "Tile Entity Data"
    );

    public static final Key<Value<Integer>> ITEM_TOOL = KeyFactory.makeSingleKey(
            TypeToken.of(Integer.class),
            new TypeToken<Value<Integer>>(){},
            DataQuery.of("MineCityTool"), "minecity:tool", "Item Tool Data"
    );

    private MineCityKeys()
    {}
}
