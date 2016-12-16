package br.com.gamemods.minecity.sponge.data.value;

import br.com.gamemods.minecity.reactive.game.item.data.ItemData;
import br.com.gamemods.minecity.sponge.data.manipulator.reactive.SpongeManipulator;
import org.spongepowered.api.item.ItemType;

import java.util.Optional;

public class SpongeItemData implements ItemData
{
    private final SpongeManipulator manipulator;
    private final ItemType item;

    public SpongeItemData(SpongeManipulator manipulator, ItemType item)
    {
        this.manipulator = manipulator;
        this.item = item;
    }

    @Override
    public Optional<String> getItemIdName()
    {
        return Optional.of(item.getName());
    }

    @Override
    public Optional<Integer> getItemId()
    {
        return Optional.empty();
    }

    @Override
    public Object getItem()
    {
        return item;
    }

    @Override
    public String toString()
    {
        return "SpongeItemData{"+
                "item="+item+
                '}';
    }
}
