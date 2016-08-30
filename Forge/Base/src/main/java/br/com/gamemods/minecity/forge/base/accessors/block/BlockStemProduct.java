package br.com.gamemods.minecity.forge.base.accessors.block;

import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.accessors.world.IWorldServer;
import br.com.gamemods.minecity.forge.base.command.ForgePlayer;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.ForgeInterfaceTransformer;
import br.com.gamemods.minecity.forge.base.protection.reaction.*;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Referenced(at = ForgeInterfaceTransformer.class)
public interface BlockStemProduct extends IBlock
{
    default boolean isValidStem(IWorldServer world, int x, int y, int z)
    {
        IState state = world.getIState(x, y, z);
        IBlock block = state.getIBlock();
        if(!(block instanceof IBlockStem))
            return false;

        IBlockStem stem = (IBlockStem) block;
        IItemStack seed = stem.getISeed(state, world, x, y, z);
        return isValidSeed(seed);
    }

    default boolean isValidSeed(IItemStack seed)
    {
        String name = seed.getIItem().getUnlocalizedName();
        String blockName = getUnlocalizedName();
        name = name.substring(name.lastIndexOf('.')+1);
        blockName = blockName.substring(blockName.lastIndexOf('.')+1);

        return name.equals("seeds_"+blockName);
    }

    @Override
    default Reaction reactBlockBreak(ForgePlayer<?, ?, ?> player, IState state, BlockPos pos)
    {
        IWorldServer world = pos.world.getInstance();
        List<BlockPos> list = Direction.cardinal.stream()
                .filter(dir -> isValidStem(world, pos.x + dir.x, pos.y, pos.z + dir.z))
                .map(pos::add)
                .collect(Collectors.toList());

        int size = list.size();
        TriggeredReaction react;
        if(size == 0)
            react = new SingleBlockReaction(pos, PermissionFlag.MODIFY);
        else if(size == 1)
            react = new DoubleBlockReaction(PermissionFlag.HARVEST, pos, list.get(0));
        else
        {
            list.add(pos);
            react = new MultiBlockReaction(PermissionFlag.HARVEST, list);
        }

        AtomicBoolean done = new AtomicBoolean();
        MineCityForge mod = player.getServer();
        react.addAllowListener((reaction, permissible, flag, pos1, message) -> {
            if(done.get())
                return;

            mod.addItemConsumer(pos.toEntity(), 2, Integer.MAX_VALUE, 2, (item, remaining) -> {
                IItemStack stack = item.getStack();
                if(stack.getIItem().isHarvest(stack))
                    item.allowToPickup(player.identity());
                return 0;
            });
        });

        return react;
    }

    @Override
    default boolean isHarvest()
    {
        return true;
    }
}
