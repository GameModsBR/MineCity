package br.com.gamemods.minecity.reactive.script;

import br.com.gamemods.minecity.reactive.ReactiveLayer;
import br.com.gamemods.minecity.reactive.game.block.ReactiveBlockType;
import br.com.gamemods.minecity.reactive.game.block.data.BlockTypeData;
import br.com.gamemods.minecity.reactive.vanilla.block.ReactiveBlockTypeClickable;
import br.com.gamemods.minecity.reactive.vanilla.block.ReactiveBlockTypeContainer;
import br.com.gamemods.minecity.reactive.vanilla.block.ReactiveBlockTypeModifiable;
import groovy.lang.Closure;
import groovy.lang.Script;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class ReactiveScript extends Script
{
    public static ReactiveBlockTypeClickable clickableBlock = ReactiveBlockTypeClickable.INSTANCE;
    public static ReactiveBlockTypeContainer containerBlock = ReactiveBlockTypeContainer.INSTANCE;
    public static ReactiveBlockTypeModifiable modifiableBlock = ReactiveBlockTypeModifiable.INSTANCE;
    public static ReactiveBlockType decorativeBlock = ReactiveBlockType.INSTANCE;

    @Nullable
    public BlockTypeData blockType(@NotNull Object block, @Nullable Closure config)
    {
        BlockTypeData data = ReactiveLayer.getBlockType(block).orElse(null);
        if(config == null || data == null)
            return data;

        config.setDelegate(data);
        config.call();
        return data;
    }

    public List<BlockTypeData> blockType(@NotNull List<?> blocks, @Nullable Closure config)
    {
        return blocks.stream()
                .map(block-> blockType(block, config))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
