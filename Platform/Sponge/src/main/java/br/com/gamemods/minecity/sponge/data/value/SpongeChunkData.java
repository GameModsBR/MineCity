package br.com.gamemods.minecity.sponge.data.value;

import br.com.gamemods.minecity.api.shape.Point;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.ChunkPos;
import br.com.gamemods.minecity.reactive.game.block.data.BlockStateData;
import br.com.gamemods.minecity.reactive.game.block.data.TileEntityData;
import br.com.gamemods.minecity.reactive.game.server.data.ChunkData;
import br.com.gamemods.minecity.reactive.game.server.data.WorldData;
import br.com.gamemods.minecity.sponge.data.manipulator.reactive.SpongeManipulator;
import com.flowpowered.math.vector.Vector3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.Item;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.extent.BlockVolume;

import java.util.Optional;
import java.util.Random;

public class SpongeChunkData implements ChunkData
{
    private static final Random rand = new Random();
    private final SpongeManipulator manipulator;
    private final Chunk chunk;
    private final ChunkPos pos;

    public SpongeChunkData(SpongeManipulator manipulator, Chunk chunk)
    {
        this.manipulator = manipulator;
        this.chunk = chunk;

        this.pos = manipulator.sponge.chunk(chunk);
    }

    @Override
    public Chunk getChunk()
    {
        return chunk;
    }

    @NotNull
    @Override
    public ChunkPos getChunkPos()
    {
        return pos;
    }

    @NotNull
    @Override
    public WorldData getWorldData()
    {
        return manipulator.server.getWorldData(chunk.getWorld());
    }

    @NotNull
    @Override
    public Optional<TileEntityData> getTileEntityData(Point pos)
    {
        return chunk.getWorld().getTileEntity(pos.x, pos.y, pos.z).map(manipulator.block::getTileEntityData);
    }

    @NotNull
    @Override
    public BlockStateData getBlockStateData(BlockPos pos)
    {
        BlockVolume volume = chunk.containsBlock(pos.x, pos.y, pos.z)? chunk : chunk.getWorld();
        return manipulator.block.getBlockStateData(volume.getBlock(pos.x, pos.y, pos.z));
    }

    @Override
    public boolean setAir(Point pos)
    {
        try
        {
            return chunk
                    .getWorld()
                    .setBlock(pos.x, pos.y, pos.z, BlockTypes.AIR.getDefaultState(), manipulator.sponge.cause());
        }
        catch(Throwable e)
        {
            manipulator.sponge.logger.error("Failed to set "+pos+" to air", e);
            return false;
        }
    }

    @Override
    public boolean setBlock(Point pos, BlockStateData state)
    {
        try
        {
            return chunk
                    .getWorld()
                    .setBlock(pos.x, pos.y, pos.z, (BlockState) state.getBlockState(), manipulator.sponge.cause());
        }
        catch(Throwable e)
        {
            manipulator.sponge.logger.error("Failed to set "+pos+" to "+state, e);
            return false;
        }
    }

    @Override
    public boolean dropAsItem(Point pos)
    {
        BlockState block = chunk.getWorld().getBlock(pos.x, pos.y, pos.z);
        if(block.getType().equals(BlockTypes.AIR))
            return false;

        ItemStack stack = ItemStack.builder().fromBlockState(block).build();
        double x = (double) (rand.nextFloat() * 0.5F)+0.25D;
        double y = (double) (rand.nextFloat() * 0.5F)+0.25D;
        double z = (double) (rand.nextFloat() * 0.5F)+0.25D;
        Item entity = (Item) chunk.getWorld().createEntity(EntityTypes.ITEM, new Vector3d(pos.x + x, pos.y + y,  pos.z + z));

        entity.offer(Keys.PICKUP_DELAY, 10);
        return entity.offer(Keys.REPRESENTED_ITEM, stack.createSnapshot()).isSuccessful()
                && setAir(pos)
                && chunk.getWorld().spawnEntity(entity, manipulator.sponge.cause());
    }

    @Override
    public boolean drop(Point pos)
    {
        try
        {
            try
            {
                return chunk.getWorld().digBlock(pos.x, pos.y, pos.z, manipulator.sponge.cause());
            }
            catch(AbstractMethodError error)
            {
                try
                {
                    World world = (World) chunk.getWorld();
                    return world.destroyBlock(new net.minecraft.util.math.BlockPos(pos.x, pos.y, pos.z), true);
                }
                catch(Throwable e)
                {
                    error.addSuppressed(e);
                    throw error;
                }
            }
        }
        catch(Throwable e)
        {
            manipulator.sponge.logger.error("Failed to drop "+pos, e);
            return false;
        }
    }

    @Override
    public String toString()
    {
        return "SpongeChunkData{"+
                "chunk="+chunk+
                ", pos="+pos+
                '}';
    }
}
