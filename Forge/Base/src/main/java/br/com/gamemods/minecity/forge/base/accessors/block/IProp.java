package br.com.gamemods.minecity.forge.base.accessors.block;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.block.IPropertyTransformer;

import java.util.Collection;

@Referenced(at = IPropertyTransformer.class)
public interface IProp<T extends Comparable<T>>
{
    String getName();

    Collection<T> getAllowedValues();

    Class<T> getValueClass();

    String getValueName(T value);
}
