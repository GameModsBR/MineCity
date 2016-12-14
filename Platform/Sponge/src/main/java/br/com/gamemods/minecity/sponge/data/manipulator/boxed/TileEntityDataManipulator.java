package br.com.gamemods.minecity.sponge.data.manipulator.boxed;

import br.com.gamemods.minecity.reactive.game.block.data.TileEntityData;

public class TileEntityDataManipulator extends SingleData<TileEntityData, TileEntityDataManipulator, TileEntityDataManipulator.Immutable>
{
    public TileEntityDataManipulator(TileEntityData value)
    {
        super(value, MineCityKeys.TILE_ENTITY_DATA);
    }

    @Override
    public TileEntityDataManipulator copy()
    {
        return new TileEntityDataManipulator(getValue());
    }

    @Override
    public Immutable asImmutable()
    {
        return new Immutable(getValue());
    }

    public static class Immutable extends SingleData.Immutable<TileEntityData, TileEntityDataManipulator, TileEntityDataManipulator.Immutable>
    {
        public Immutable(TileEntityData value)
        {
            super(value, MineCityKeys.TILE_ENTITY_DATA);
        }

        @Override
        public TileEntityDataManipulator asMutable()
        {
            return new TileEntityDataManipulator(getValue());
        }
    }
}
