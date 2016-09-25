package br.com.gamemods.minecity.forge.base.protection.thaumcraft;

import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.api.world.WorldDim;
import br.com.gamemods.minecity.forge.base.ForgeUtil;
import br.com.gamemods.minecity.forge.base.accessors.block.IBlock;
import br.com.gamemods.minecity.forge.base.accessors.block.IState;
import br.com.gamemods.minecity.forge.base.accessors.block.ITileEntity;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.item.IItem;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.accessors.world.IWorldServer;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.ModInterfacesTransformer;
import br.com.gamemods.minecity.forge.base.protection.reaction.MultiBlockReaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.NoReaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.ObservedReaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.Reaction;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Referenced(at = ModInterfacesTransformer.class)
public interface IItemElementalShovel extends IItem
{
    ArrayList<IBlockCoordinates> getArchitectBlocks(ItemStack stack, World world, int x, int y, int z, int side, EntityPlayer player);

    @Override
    default Reaction reactRightClickBlock(IEntityPlayerMP player, IItemStack stack, boolean offHand,
                                          IState state, BlockPos pos, Direction face)
    {
        IWorldServer world = pos.world.getInstance(IWorldServer.class);
        ITileEntity tile = world.getTileEntity(pos);
        if(tile != null)
            return NoReaction.INSTANCE;

        IBlock block = state.getIBlock();
        WorldDim dim = player.getServer().world(world);
        AtomicReference<BlockPos> last = new AtomicReference<>();
        boolean sneaking = player.isSneaking();
        player.setSneaking(true);
        List<BlockPos> affected = getArchitectBlocks(stack.getStack(), (World) world, pos.x, pos.y,
                pos.z, ForgeUtil.toForge(face), (EntityPlayer) player
        ).stream().map(coord-> {
            BlockPos p = coord.toPos(dim, last.get());
            last.set(p);
            return p;
        }).collect(Collectors.toList());
        player.setSneaking(sneaking);

        IItemStack fakeStack = (IItemStack) (Object) new ItemStack((Block) block, 1, state.getIntValueOrMeta("metadata"));
        return new ObservedReaction(Reaction.combine(Stream.of(
                Stream.of( MultiBlockReaction.create(PermissionFlag.MODIFY, affected) ),
                affected.stream().map(p-> block.reactPrePlace(player, fakeStack, p))
        ).flatMap(Function.identity()))).addDenyListener(m->
            affected.stream().flatMap(p-> Direction.block.stream().map(p::add)).distinct().forEach(player::sendBlock)
        ).addDenyListener(m-> player.sendInventoryContents());
    }
}
