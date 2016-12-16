package br.com.gamemods.minecity.sponge.data.value;

import br.com.gamemods.minecity.reactive.game.item.data.ItemData;
import br.com.gamemods.minecity.reactive.game.item.data.ItemStackData;
import br.com.gamemods.minecity.reactive.game.item.data.ItemStateData;
import br.com.gamemods.minecity.reactive.game.item.data.ItemTraitData;
import br.com.gamemods.minecity.sponge.data.manipulator.reactive.SpongeManipulator;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.stream.Stream;

public class SpongeItemStackData implements ItemStackData, ItemStateData
{
    private final SpongeManipulator manipulator;
    private final ItemStack stack;

    public SpongeItemStackData(SpongeManipulator manipulator, ItemStack stack)
    {
        this.manipulator = manipulator;
        this.stack = stack;
    }

    @Override
    public ItemData getItemData()
    {
        return manipulator.item.getItemData(stack.getItem());
    }

    @Override
    public ItemStateData getItemStateData()
    {
        return this;
    }

    @Override
    public Stream<ItemTraitData<?>> itemTraitStream()
    {
        return Stream.empty();
    }

    @Override
    public String toString()
    {
        return "SpongeItemStackData{"+
                "stack="+stack+
                '}';
    }
}
