package br.com.gamemods.minecity.reactive.script;

import br.com.gamemods.minecity.reactive.ReactiveLayer;
import br.com.gamemods.minecity.reactive.game.block.ReactiveBlockType;
import br.com.gamemods.minecity.reactive.game.block.data.BlockTraitData;
import br.com.gamemods.minecity.reactive.game.block.data.BlockTypeData;
import br.com.gamemods.minecity.reactive.game.item.data.ItemData;
import br.com.gamemods.minecity.reactive.vanilla.block.ReactiveBlockClickable;
import br.com.gamemods.minecity.reactive.vanilla.block.ReactiveBlockContainer;
import br.com.gamemods.minecity.reactive.vanilla.block.ReactiveBlockModifiable;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
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

    private Class<?> findClass(Object obj)
    {
        if(obj instanceof Class)
            return  (Class<?>) obj;
        else if(obj instanceof CharSequence)
            try
            {
                return Class.forName(obj.toString());
            }
            catch(ClassNotFoundException ignored)
            {
            }

        return null;
    }

    public ItemData itemType(@NotNull Object item, @Nullable @DelegatesTo(ItemData.class) Closure config)
    {
        ItemData data = ReactiveLayer.getItemData(item).orElse(null);
        if(data == null && config != null)
        {
            Class<?> clazz = findClass(item);
            if(clazz != null)
            {
                ReactiveLayer.getItemManipulator().findItemTypes(clazz)
                        .forEach(itemTypeData ->
                        {
                            Closure clone = (Closure) config.clone();
                            clone.setDelegate(itemTypeData);
                            clone.call();
                        });
                return null;
            }
        }

        if(config == null || data == null)
            return data;

        config.setDelegate(data);
        config.call();
        return data;
    }

    public List<ItemData> itemType(@NotNull List<?> items, @Nullable @DelegatesTo(ItemData.class) Closure config)
    {
        return items.stream()
                .map(item-> itemType(item, config))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Nullable
    public BlockTypeData blockType(@NotNull Object block, @Nullable @DelegatesTo(BlockTypeData.class) Closure config)
    {
        BlockTypeData data = ReactiveLayer.getBlockType(block).orElse(null);
        if(data == null && config != null)
        {
            Class<?> clazz = findClass(block);
            if(clazz != null)
            {
                ReactiveLayer.getBlockManipulator().findBlockTypes(clazz)
                        .forEach(blockTypeData ->
                        {
                            Closure clone = (Closure) config.clone();
                            clone.setDelegate(blockTypeData);
                            clone.call();
                        });
                return null;
            }
        }

        if(config == null || data == null)
            return data;

        config.setDelegate(data);
        config.call();
        return data;
    }

    public List<BlockTypeData> blockType(@NotNull List<?> blocks, @Nullable @DelegatesTo(BlockTypeData.class) Closure config)
    {
        return blocks.stream()
                .map(block-> blockType(block, config))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public BlockTraitData<?> blockTrait(@NotNull Object trait, @Nullable @DelegatesTo(BlockTraitData.class) Closure config)
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

    public List<BlockTraitData<?>> blockTrait(@NotNull List<?> traits, @Nullable @DelegatesTo(BlockTraitData.class) Closure config)
    {
        return traits.stream()
                .map(trait-> blockTrait(trait, config))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
