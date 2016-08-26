package br.com.gamemods.minecity.forge.base.accessors.block;

import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.accessors.entity.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.item.IItem;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.accessors.world.IWorldServer;
import br.com.gamemods.minecity.forge.base.command.ForgePlayer;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.block.BlockTransformer;
import br.com.gamemods.minecity.forge.base.protection.reaction.NoReaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.Reaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.SingleBlockReaction;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraftforge.fluids.BlockFluidBase;

@Referenced(at = BlockTransformer.class)
public interface IBlock
{
    default Block getForgeBlock()
    {
        return (Block) this;
    }

    IState getDefaultIState();

    default boolean isLiquid()
    {
        return this instanceof BlockLiquid || this instanceof BlockFluidBase;
    }

    default int getId()
    {
        return Block.getIdFromBlock((Block) this);
    }

    IItem getItem();

    default Reaction reactRightClick(BlockPos pos, IState state, IEntityPlayerMP player, IItemStack stack, boolean offHand, Direction face)
    {
        return NoReaction.INSTANCE;
    }

    default Reaction reactBlockPlace(ForgePlayer<?,?,?> player, IBlockSnapshot snap)
    {
        return new SingleBlockReaction(snap.getPosition(player.getServer()), PermissionFlag.MODIFY);
    }

    default Reaction reactBlockBreak(ForgePlayer<?,?,?> player, IState state, BlockPos pos)
    {
        return new SingleBlockReaction(pos, PermissionFlag.MODIFY);
    }

    boolean isReplaceable(IWorldServer world, int x, int y, int z);

    default boolean isReplaceable(BlockPos pos)
    {
        assert pos.world.instance != null;
        return isReplaceable((IWorldServer) pos.world.instance, pos.x, pos.y, pos.z);
    }

    default String getUnlocalizedName()
    {
        return getForgeBlock().getUnlocalizedName();
    }
}
