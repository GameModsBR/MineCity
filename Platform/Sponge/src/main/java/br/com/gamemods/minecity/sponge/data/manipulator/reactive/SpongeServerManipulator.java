package br.com.gamemods.minecity.sponge.data.manipulator.reactive;

import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.ChunkPos;
import br.com.gamemods.minecity.reactive.game.server.ReactiveChunk;
import br.com.gamemods.minecity.reactive.game.server.data.ChunkData;
import br.com.gamemods.minecity.reactive.game.server.data.ServerData;
import br.com.gamemods.minecity.reactive.game.server.data.ServerManipulator;
import br.com.gamemods.minecity.reactive.game.server.data.WorldData;
import br.com.gamemods.minecity.reactive.reactor.ServerReactor;
import br.com.gamemods.minecity.sponge.data.value.SpongeChunkData;
import br.com.gamemods.minecity.sponge.data.value.SpongeServerData;
import br.com.gamemods.minecity.sponge.data.value.SpongeWorldData;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.World;

import java.util.Optional;

public class SpongeServerManipulator implements ServerManipulator, ServerReactor
{
    private final SpongeManipulator manipulator;
    private final SpongeServerData serverData;

    public SpongeServerManipulator(SpongeManipulator manipulator)
    {
        this.manipulator = manipulator;
        serverData = new SpongeServerData(manipulator, Sponge.getServer());
    }

    @NotNull
    @Override
    public Optional<ChunkData> getChunkData(@NotNull Object chunk)
    {
        if(chunk instanceof BlockPos)
            chunk = ((BlockPos) chunk).getChunk();

        if(chunk instanceof ChunkPos)
        {
            ChunkPos pos = (ChunkPos) chunk;
            return manipulator.sponge.world(pos.world)
                    .flatMap(world -> world.getChunk(pos.x, 0, pos.z))
                    .map(this::getChunkData);
        }

        if(!(chunk instanceof Chunk))
            return Optional.empty();

        return Optional.of(getChunkData((Chunk) chunk));
    }

    @NotNull
    public ChunkData getChunkData(@NotNull Chunk chunk)
    {
        // TODO Find a way to cache this alongside with the Chunk, ChunkPos or ClaimedChunk objects
        return new SpongeChunkData(manipulator, chunk);
    }

    @NotNull
    @Override
    public Optional<ReactiveChunk> getReactiveChunk(Object chunk)
    {
        return Optional.empty();
    }

    @NotNull
    @Override
    public Optional<WorldData> getWorldData(@NotNull Object world)
    {
        if(!(world instanceof World))
            return Optional.empty();

        return Optional.of(getWorldData((World) world));
    }

    public WorldData getWorldData(@NotNull World world)
    {
        // TODO Find a way to cache this alongside the world object
        return new SpongeWorldData(manipulator, world);
    }

    @NotNull
    @Override
    public Optional<ServerData> getServerData(@NotNull Object server)
    {
        if(!serverData.server.equals(server))
            return Optional.empty();

        return Optional.of(serverData);
    }

    @NotNull
    public ServerData getServerData(Server server)
    {
        if(serverData.server.equals(server))
            return serverData;

        return new SpongeServerData(manipulator, server);
    }

    @Override
    public String toString()
    {
        return "SpongeServerManipulator{"+
                "manipulator="+manipulator+
                ", serverData="+serverData+
                '}';
    }
}
