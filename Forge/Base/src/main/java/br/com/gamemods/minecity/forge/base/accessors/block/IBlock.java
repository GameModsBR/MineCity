package br.com.gamemods.minecity.forge.base.accessors.block;

import br.com.gamemods.minecity.api.permission.Permissible;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntity;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.item.IItem;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.accessors.world.IWorldServer;
import br.com.gamemods.minecity.forge.base.command.ForgePlayer;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.ForgeInterfaceTransformer;
import br.com.gamemods.minecity.forge.base.protection.reaction.MultiBlockReaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.NoReaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.Reaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.SingleBlockReaction;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraftforge.fluids.BlockFluidBase;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Referenced(at = ForgeInterfaceTransformer.class)
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

    List<IItemStack> getDrops(IWorldServer world, IState state, int fortune, int x, int y, int z);

    default Reaction reactRightClick(BlockPos pos, IState state, IEntityPlayerMP player, IItemStack stack, boolean offHand, Direction face)
    {
        return NoReaction.INSTANCE;
    }

    default Reaction reactPrePlace(Permissible who, IItemStack stack, BlockPos pos)
    {
        return new SingleBlockReaction(pos, PermissionFlag.MODIFY);
    }

    default Reaction reactBlockPlace(ForgePlayer<?, ?, ?> player, IBlockSnapshot snap, IItemStack hand, boolean offHand)
    {
        return NoReaction.INSTANCE;
    }

    default Reaction reactBlockBreak(ForgePlayer<?,?,?> player, IState state, BlockPos pos)
    {
        return new SingleBlockReaction(pos, PermissionFlag.MODIFY);
    }

    default Reaction reactPlayerModifyWithProjectile(Permissible player, IEntity projectile,
                                         IState state, IWorldServer world, BlockPos pos)
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

    default boolean isHarvest()
    {
        return false;
    }

    IItem getItemDropped(IState state, Random rand, int fortune);

    IItemStack getItemStack(IState state, IWorldServer world, int x, int y, int z);

    default Reaction reactBoneMeal(IEntityPlayerMP entity, BlockPos pos, IState state)
    {
        return new SingleBlockReaction(pos, PermissionFlag.HARVEST);
    }

    default Reaction reactRightClickAsItem(IEntityPlayerMP player, IItemStack stack, boolean offHand, IState state, BlockPos pos, Direction face)
    {
        return NoReaction.INSTANCE;
    }

    default Reaction reactBlockGrow(MineCityForge mod, IState state, BlockPos block, List<IBlockSnapshot> changes, IEntityPlayerMP boneMealPlayer)
    {
        return new MultiBlockReaction(PermissionFlag.MODIFY, changes.stream().map(snap-> snap.getPosition(mod)).collect(Collectors.toList()));
    }

    float getHardness(IState state, IWorldServer world, int x, int y, int z);

    default float getHardness(IState state, BlockPos pos)
    {
        return getHardness(state, pos.world.getInstance(IWorldServer.class), pos.x, pos.y, pos.z);
    }
}
