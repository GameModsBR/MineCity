package br.com.gamemods.minecity.forge.base.accessors;

import br.com.gamemods.minecity.api.PlayerID;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.UsernameCache;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface IMinecraftServer
{
    default MinecraftServer getServer()
    {
        return (MinecraftServer) this;
    }

    default Map<UUID, String> getUsernameCache()
    {
        return UsernameCache.getMap();
    }

    default IPlayerList getIPlayerList()
    {
        return (IPlayerList) ((MinecraftServer) this).getPlayerList();
    }

    default PlayerID getPlayerId(UUID uuid)
    {
        String name = UsernameCache.getLastKnownUsername(uuid);
        return new PlayerID(uuid, name != null? name : "???");
    }

    @Nullable
    default PlayerID getPlayerId(String name)
    {
        Optional<UUID> uuid = getUsernameCache().entrySet().parallelStream()
                .filter(e-> e.getValue().equalsIgnoreCase(name)).map(Map.Entry::getKey).findAny();
        if(uuid.isPresent())
            return new PlayerID(uuid.get(), name);

        return null;
    }
}
