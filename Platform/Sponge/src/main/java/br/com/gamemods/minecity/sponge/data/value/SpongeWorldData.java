package br.com.gamemods.minecity.sponge.data.value;

import br.com.gamemods.minecity.api.shape.Point;
import br.com.gamemods.minecity.api.shape.PrecisePoint;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.WorldDim;
import br.com.gamemods.minecity.reactive.game.server.data.ServerData;
import br.com.gamemods.minecity.reactive.game.server.data.WorldData;
import br.com.gamemods.minecity.sponge.data.manipulator.reactive.SpongeManipulator;
import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3f;
import com.flowpowered.math.vector.Vector3i;
import com.flowpowered.math.vector.Vector3l;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.extent.Extent;

import java.util.Optional;

public class SpongeWorldData implements WorldData
{
    private static boolean enableNms = true;
    private final SpongeManipulator manipulator;
    private final World world;

    public SpongeWorldData(SpongeManipulator manipulator, World world)
    {
        this.manipulator = manipulator;
        this.world = world;
    }

    @Override
    public World getWorld()
    {
        return world;
    }

    public WorldDim getDim()
    {
        return manipulator.sponge.world(world);
    }

    @Override
    public Optional<BlockPos> getBlockPos(Object pos)
    {
        if(pos instanceof BlockPos)
            return Optional.of((BlockPos) pos);
        if(pos instanceof Location)
        {
            Location<?> loc = (Location) pos;
            Extent extent = loc.getExtent();
            if(world.equals(extent) || extent instanceof Chunk && ((Chunk) extent).getWorld().equals(world))
                return Optional.of(new BlockPos(getDim(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));

            return Optional.empty();
        }
        if(pos instanceof Point)
        {
            Point point = (Point) pos;
            return Optional.of(new BlockPos(getDim(), point.x, point.y, point.z));
        }
        if(pos instanceof PrecisePoint)
        {
            PrecisePoint point = (PrecisePoint) pos;
            return Optional.of(new BlockPos(getDim(), (int)point.x, (int)point.y, (int)point.z));
        }
        if(pos instanceof Vector3i)
        {
            Vector3i point = (Vector3i) pos;
            return Optional.of(new BlockPos(getDim(), point.getX(), point.getY(), point.getZ()));
        }
        if(pos instanceof Vector3d)
        {
            Vector3d point = (Vector3d) pos;
            return Optional.of(new BlockPos(getDim(), (int) point.getX(), (int) point.getY(), (int) point.getZ()));
        }
        if(pos instanceof Vector3f)
        {
            Vector3f point = (Vector3f) pos;
            return Optional.of(new BlockPos(getDim(), (int) point.getX(), (int) point.getY(), (int) point.getZ()));
        }
        if(pos instanceof Vector3l)
        {
            Vector3l point = (Vector3l) pos;
            return Optional.of(new BlockPos(getDim(), (int) point.getX(), (int) point.getY(), (int) point.getZ()));
        }

        if(enableNms)
            try
            {
                if(pos instanceof net.minecraft.util.math.BlockPos)
                {
                    net.minecraft.util.math.BlockPos nmsPos = (net.minecraft.util.math.BlockPos) pos;
                    return Optional.of(new BlockPos(getDim(), nmsPos.getX(), nmsPos.getY(), nmsPos.getZ()));
                }
            }
            catch(Error error)
            {
                enableNms = false;
                manipulator.sponge.logger.error("Failed to check if "+pos+" is a NMS BlockPos. Disabling NMS checks!", error);
            }

        return Optional.empty();
    }

    @NotNull
    @Override
    public ServerData getServerData()
    {
        return manipulator.server.getServerData(Sponge.getServer());
    }

    @Override
    public String toString()
    {
        return "SpongeWorldData{"+
                "world="+world+
                '}';
    }
}
