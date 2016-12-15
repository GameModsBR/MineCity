package br.com.gamemods.minecity.reactive.game.item.data;

import br.com.gamemods.minecity.reactive.game.item.data.supplier.SupplierItemData;

import java.util.Optional;

/**
 * A raw item without any property
 */
public interface ItemData
{
    Optional<String> getItemIdName();
    Optional<Integer> getItemId();
    Object getItem();

    default boolean matches(Object data)
    {
        if(equals(data))
            return true;

        if(data instanceof CharSequence)
            return getItemIdName().map(data.toString()::equals).orElse(false);

        //noinspection SimplifiableIfStatement
        if(data instanceof SupplierItemData)
            return equals(((SupplierItemData) data).getItemData());

        return false;
    }
}
