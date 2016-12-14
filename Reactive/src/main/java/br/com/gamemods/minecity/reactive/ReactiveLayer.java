package br.com.gamemods.minecity.reactive;

import br.com.gamemods.minecity.reactive.game.block.data.*;
import br.com.gamemods.minecity.reactive.game.block.data.supplier.SupplierBlockStateData;
import br.com.gamemods.minecity.reactive.game.block.data.supplier.SupplierBlockTypeData;
import br.com.gamemods.minecity.reactive.game.block.data.supplier.SupplierTileEntityData;
import br.com.gamemods.minecity.reactive.game.entity.data.EntityData;
import br.com.gamemods.minecity.reactive.game.entity.data.EntityManipulator;
import br.com.gamemods.minecity.reactive.game.entity.data.supplier.SupplierEntityData;
import br.com.gamemods.minecity.reactive.game.item.data.ItemManipulator;
import br.com.gamemods.minecity.reactive.game.server.data.ChunkData;
import br.com.gamemods.minecity.reactive.game.server.data.ServerData;
import br.com.gamemods.minecity.reactive.game.server.data.ServerManipulator;
import br.com.gamemods.minecity.reactive.game.server.data.supplier.SupplierChunkData;
import br.com.gamemods.minecity.reactive.game.server.data.supplier.SupplierServerData;
import br.com.gamemods.minecity.reactive.reactor.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

public class ReactiveLayer
{
    @Nullable
    private static Reactor reactor;

    @Nullable
    private static Manipulator manipulator;

    @NotNull
    public static Reactor getReactor()
    {
        return Objects.requireNonNull(reactor, "No reactor was registered");
    }

    public static void setReactor(@NotNull Reactor reactor)
    {
        ReactiveLayer.reactor = Objects.requireNonNull(reactor, "Cannot set the reactor to null");
    }

    @NotNull
    public static Manipulator getManipulator()
    {
        return Objects.requireNonNull(manipulator, "No data manipulator was registered");
    }

    public static void setManipulator(@NotNull Manipulator manipulator)
    {
        ReactiveLayer.manipulator = Objects.requireNonNull(manipulator, "Cannot set the manipulator to null");
    }

    @NotNull
    public static Optional<BlockTypeData> getBlockType(@NotNull Object block)
    {
        if(block instanceof SupplierBlockTypeData)
            return Optional.of(((SupplierBlockTypeData) block).getBlockTypeData());

        return getBlockManipulator().getBlockType(block);
    }

    @NotNull
    public static Optional<BlockStateData> getBlockState(@NotNull Object blockState)
    {
        if(blockState instanceof SupplierBlockStateData)
            return Optional.of(((SupplierBlockStateData) blockState).getBlockStateData());

        return getBlockManipulator().getBlockState(blockState);
    }

    @NotNull
    public static Optional<TileEntityData> getTileEntity(@NotNull Object tileEntity)
    {
        if(tileEntity instanceof SupplierTileEntityData)
            return Optional.of(((SupplierTileEntityData) tileEntity).getTileEntityData());

        return getBlockManipulator().getTileEntity(tileEntity);
    }

    @NotNull
    public static Optional<BlockTraitData<?>> getBlockTrait(@NotNull Object blockTrait)
    {
        if(blockTrait instanceof BlockTraitData)
            return Optional.of(((BlockTraitData<?>) blockTrait).getBlockTraitData());

        return getBlockManipulator().getBlockTrait(blockTrait);
    }

    @NotNull
    public static Optional<EntityData> getEntityData(@NotNull Object entity)
    {
        if(entity instanceof SupplierEntityData)
            return Optional.of(((SupplierEntityData) entity).getEntityData());

        return getEntityManipulator().getEntityData(entity);
    }

    @NotNull
    public static Optional<ChunkData> getChunk(@NotNull Object chunk)
    {
        if(chunk instanceof SupplierChunkData)
            return Optional.of(((SupplierChunkData) chunk).getChunkData());

        return getServerManipulator().getChunkData(chunk);
    }

    @NotNull
    public static Optional<ServerData> getServerData(@NotNull Object server)
    {
        if(server instanceof SupplierServerData)
            return Optional.of(((SupplierServerData) server).getServerData());

        return getServerManipulator().getServerData(server);
    }

    @NotNull
    public static BlockManipulator getBlockManipulator()
    {
        return getManipulator().getBlockManipulator();
    }

    @NotNull
    public static ItemManipulator getItemManipulator()
    {
        return getManipulator().getItemManipulator();
    }

    @NotNull
    public static EntityManipulator getEntityManipulator()
    {
        return getManipulator().getEntityManipulator();
    }

    @NotNull
    public static ServerManipulator getServerManipulator()
    {
        return getManipulator().getServerManipulator();
    }

    @NotNull
    public static BlockReactor getBlockReactor()
    {
        return getReactor().getBlockReactor();
    }

    @NotNull
    public static ItemReactor getItemReactor()
    {
        return getReactor().getItemReactor();
    }

    @NotNull
    public static EntityReactor getEntityReactor()
    {
        return getReactor().getEntityReactor();
    }

    @NotNull
    public static ServerReactor getServerReactor()
    {
        return getReactor().getServerReactor();
    }

    private ReactiveLayer()
    {}
}
