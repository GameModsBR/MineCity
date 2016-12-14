package br.com.gamemods.minecity.forge.mc_1_7_10.accessors.block;

import br.com.gamemods.minecity.forge.base.accessors.block.IProp;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public class SevenMetadataProperty implements IProp<Integer>
{
    public static final SevenMetadataProperty INSTANCE = new SevenMetadataProperty();
    public static final Set<IProp<?>> SINGLETON = Collections.singleton(INSTANCE);

    @Override
    public String getName()
    {
        return "metadata";
    }

    @Override
    public Collection<Integer> getAllowedValues()
    {
        return Collections.emptyList();
    }

    @Override
    public Class<Integer> getValueClass()
    {
        return Integer.class;
    }

    @Override
    public String getValueName(Integer value)
    {
        return String.valueOf(value);
    }
}
