package br.com.gamemods.minecity.forge.mc_1_7_10.accessors.block;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.accessors.block.IBlockStem;
import br.com.gamemods.minecity.forge.base.accessors.block.IState;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.accessors.world.IWorldServer;
import br.com.gamemods.minecity.forge.mc_1_7_10.core.transformer.forge.SevenInterfaceTransformer;
import net.minecraft.block.BlockStem;
import net.minecraft.world.World;

@Referenced(at = SevenInterfaceTransformer.class)
public interface SevenBlockStem extends IBlockStem, SevenBlock
{
    @Override
    default IItemStack getISeed(IState state, IWorldServer world, int x, int y, int z)
    {
        return (IItemStack) (Object) ((BlockStem) this).getDrops((World) world, x, y, z, 16, 0).get(0);
    }
}
