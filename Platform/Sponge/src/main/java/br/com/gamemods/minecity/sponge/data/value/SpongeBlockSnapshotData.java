package br.com.gamemods.minecity.sponge.data.value;

import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.reactive.game.block.data.BlockSnapshotData;
import br.com.gamemods.minecity.reactive.game.block.data.BlockStateData;
import br.com.gamemods.minecity.reactive.game.block.data.TileEntityData;
import br.com.gamemods.minecity.sponge.data.manipulator.reactive.SpongeManipulator;
import com.flowpowered.math.vector.Vector3i;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Optional;
import java.util.UUID;

public class SpongeBlockSnapshotData implements BlockSnapshotData
{
    private final SpongeManipulator manipulator;
    private final BlockSnapshot snapshot;

    public SpongeBlockSnapshotData(SpongeManipulator manipulator, BlockSnapshot snapshot)
    {
        this.manipulator = manipulator;
        this.snapshot = snapshot;
    }

    @Override
    public Object getBlockSnapshot()
    {
        return snapshot;
    }

    @Override
    public BlockPos getPosition()
    {
        Optional<Location<World>> location = snapshot.getLocation();
        if(location.isPresent())
        {
            Location<World> loc = location.get();
            return new BlockPos(manipulator.sponge.world(loc.getExtent()), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        }

        World world = Sponge.getServer().getWorld(snapshot.getWorldUniqueId())
                .orElseThrow(()-> new UnsupportedOperationException("The world "+snapshot.getWorldUniqueId()+" is not loaded"))
                ;

        Vector3i pos = snapshot.getPosition();
        return new BlockPos(manipulator.sponge.world(world), pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public Optional<TileEntityData> getTileEntityData()
    {
        // TODO Implement
        return Optional.empty();
    }

    @Override
    public Optional<BlockStateData> getExtendedBlockStateData()
    {
        return Optional.of(manipulator.block.getBlockStateData(snapshot.getExtendedState()));
    }

    @Override
    public Optional<UUID> getCreatorUUID()
    {
        return snapshot.getCreator();
    }

    @Override
    public Optional<UUID> getNotifierUUID()
    {
        return snapshot.getNotifier();
    }

    @NotNull
    @Override
    public BlockStateData getBlockStateData()
    {
        return manipulator.block.getBlockStateData(snapshot.getState());
    }

    @Override
    public String toString()
    {
        return "SpongeBlockSnapshotData{"+
                "snapshot="+snapshot+
                '}';
    }
}
