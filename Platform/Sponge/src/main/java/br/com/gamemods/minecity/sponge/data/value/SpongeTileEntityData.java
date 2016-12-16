package br.com.gamemods.minecity.sponge.data.value;

import br.com.gamemods.minecity.reactive.game.block.data.TileEntityData;
import br.com.gamemods.minecity.sponge.data.manipulator.reactive.SpongeManipulator;
import org.spongepowered.api.block.tileentity.TileEntity;

public class SpongeTileEntityData implements TileEntityData
{
    private final SpongeManipulator manipulator;
    private final TileEntity tileEntity;

    public SpongeTileEntityData(SpongeManipulator manipulator, TileEntity tileEntity)
    {
        this.manipulator = manipulator;
        this.tileEntity = tileEntity;
    }

    @Override
    public TileEntity getTileEntity()
    {
        return tileEntity;
    }

    @Override
    public String toString()
    {
        return "SpongeTileEntityData{"+
                "tileEntity="+tileEntity+
                '}';
    }
}
