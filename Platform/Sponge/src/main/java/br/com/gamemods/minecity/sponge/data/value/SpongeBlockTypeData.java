package br.com.gamemods.minecity.sponge.data.value;

import br.com.gamemods.minecity.reactive.game.block.data.BlockTypeData;
import br.com.gamemods.minecity.sponge.data.manipulator.reactive.SpongeManipulator;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.block.BlockType;

import java.util.Optional;

public class SpongeBlockTypeData implements BlockTypeData
{
    private final SpongeManipulator manipulator;
    private final BlockType blockType;

    public SpongeBlockTypeData(SpongeManipulator manipulator, BlockType blockType)
    {
        this.manipulator = manipulator;
        this.blockType = blockType;
    }

    @Override
    public BlockType getBlockType()
    {
        return blockType;
    }

    @NotNull
    @Override
    public Optional<String> getBlockIdName()
    {
        return Optional.of(blockType.getName());
    }

    @NotNull
    @Override
    public Optional<Integer> getBlockId()
    {
        return Optional.empty();
    }
}
