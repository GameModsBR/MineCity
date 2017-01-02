package br.com.gamemods.minecity.reactive.script;

import br.com.gamemods.minecity.reactive.ReactiveLayer;
import br.com.gamemods.minecity.reactive.game.block.ReactiveBlockType;
import br.com.gamemods.minecity.reactive.game.block.data.BlockTraitData;
import br.com.gamemods.minecity.reactive.game.block.data.BlockTypeData;
import br.com.gamemods.minecity.reactive.vanilla.block.ReactiveBlockClickable;
import br.com.gamemods.minecity.reactive.vanilla.block.ReactiveBlockContainer;
import br.com.gamemods.minecity.reactive.vanilla.block.ReactiveBlockModifiable;
import groovy.lang.Closure;
import groovy.lang.Script;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class ReactiveScript extends Script
{
    public static ReactiveBlockClickable clickableBlock = ReactiveBlockClickable.INSTANCE;
    public static ReactiveBlockContainer containerBlock = ReactiveBlockContainer.INSTANCE;
    public static ReactiveBlockModifiable modifiableBlock = ReactiveBlockModifiable.INSTANCE;
    public static ReactiveBlockType decorativeBlock = ReactiveBlockType.DECORATIVE;

    @Nullable
    public BlockTypeData blockType(@NotNull Object block, @Nullable Closure config)
    {
        BlockTypeData data = ReactiveLayer.getBlockType(block).orElse(null);
        if(data == null && config != null && block instanceof CharSequence)
            try
            {
                ReactiveLayer.getBlockManipulator().findBlockTypes(Class.forName(block.toString()))
                        .forEach(blockTypeData -> {
                            Closure clone = (Closure) config.clone();
                            clone.setDelegate(blockTypeData);
                            clone.call();
                        });
                return null;
            }
            catch(ClassNotFoundException ignored)
            {
            }

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

    public BlockTraitData<?> blockTrait(@NotNull Object trait, @Nullable Closure config)
    {
        BlockTraitData<?> data = ReactiveLayer.getBlockTrait(trait).orElse(null);
        if(config == null || data == null)
        {
            System.err.println("Block trait: data == null! Trait:"+trait);
            return data;
        }

        config.setDelegate(data);
        config.call();
        return data;
    }

    public List<BlockTraitData<?>> blockTrait(@NotNull List<?> traits, @Nullable Closure config)
    {
        return traits.stream()
                .map(trait-> blockTrait(trait, config))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
