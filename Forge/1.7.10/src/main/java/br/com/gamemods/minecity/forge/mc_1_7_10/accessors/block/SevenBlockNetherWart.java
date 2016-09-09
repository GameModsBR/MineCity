package br.com.gamemods.minecity.forge.mc_1_7_10.accessors.block;

import br.com.gamemods.minecity.forge.base.accessors.block.IBlockNetherWart;
import br.com.gamemods.minecity.forge.base.accessors.item.IItem;
import br.com.gamemods.minecity.forge.base.accessors.world.IWorldServer;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.mc_1_7_10.core.transformer.forge.SevenInterfaceTransformer;
import net.minecraft.init.Items;

@Referenced(at = SevenInterfaceTransformer.class)
public interface SevenBlockNetherWart extends IBlockNetherWart
{
    @Override
    default IItem getISeed(IWorldServer world)
    {
        return (IItem) Items.nether_wart;
    }
}
