package br.com.gamemods.minecity.forge.mc_1_7_10.accessors.block;

import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.accessors.block.IBlockSnapshot;
import br.com.gamemods.minecity.forge.base.accessors.block.IState;
import br.com.gamemods.minecity.forge.base.accessors.world.IWorldServer;
import br.com.gamemods.minecity.forge.mc_1_7_10.core.transformer.forge.SevenInterfaceTransformer;
import net.minecraft.block.Block;
import net.minecraftforge.common.util.BlockSnapshot;

@Referenced(at = SevenInterfaceTransformer.class)
public interface SevenBlockSnapshot extends IBlockSnapshot
{
    @Override
    default BlockPos getPosition(MineCityForge mod)
    {
        BlockSnapshot snapshot = (BlockSnapshot) this;
        return new BlockPos(mod.world(snapshot.world), snapshot.x, snapshot.y, snapshot.z);
    }

    @Override
    default int getX()
    {
        return ((BlockSnapshot) this).x;
    }

    @Override
    default int getY()
    {
        return ((BlockSnapshot) this).y;
    }

    @Override
    default int getZ()
    {
        return ((BlockSnapshot) this).z;
    }

    @Override
    default IWorldServer getIWorld()
    {
        return (IWorldServer) ((BlockSnapshot) this).world;
    }

    @Override
    default IState getCurrentState()
    {
        BlockSnapshot snapshot = (BlockSnapshot) this;
        Block currentBlock = snapshot.getCurrentBlock();
        int meta = snapshot.world.getBlockMetadata(snapshot.x, snapshot.y, snapshot.z);
        if(meta == 0)
            return (IState) currentBlock;
        return new SevenBlockState((SevenBlock) currentBlock, meta);
    }

    @Override
    default IState getReplacedState()
    {
        BlockSnapshot snapshot = (BlockSnapshot) this;
        Block replacedBlock = snapshot.getReplacedBlock();
        int meta = snapshot.meta;
        if(meta == 0)
            return (IState) replacedBlock;
        return new SevenBlockState((SevenBlock) replacedBlock, meta);
    }
}
