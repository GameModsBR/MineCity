package br.com.gamemods.minecity.api;

import br.com.gamemods.minecity.api.permission.OptionalPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.Function;

public final class PlayerID extends OptionalPlayer<UUID>
{
    private static CacheMap<UUID, PlayerID> cache = new CacheMap<>(100);
    private static final long serialVersionUID = -1690881554834873995L;

    @Nullable
    public static PlayerID get(UUID uuid)
    {
        return cache.get(uuid);
    }

    @Nullable
    public static PlayerID get(UUID playerId, Function<UUID, PlayerID> creator)
    {
        return cache.computeIfAbsent(playerId, creator);
    }

    @SuppressWarnings("deprecation")
    @NotNull
    public static PlayerID get(UUID playerId, String name)
    {
        return cache.computeIfAbsent(playerId, uuid-> new PlayerID(uuid, name));
    }

    @Deprecated
    public PlayerID(@NotNull UUID uniqueId, @NotNull String name)
    {
        super(uniqueId, name);
    }

    public PlayerID(int dataSourceId, @NotNull UUID uniqueId, @NotNull String name)
    {
        super(uniqueId, name);
        setDataSourceId(dataSourceId);
    }

    @Override
    public Type getType()
    {
        return Type.PLAYER;
    }

    @Override
    public String toString()
    {
        return "PlayerID{" +
                "name='" + getName() + '\'' +
                ", uniqueId=" + uniqueId +
                '}';
    }
}
