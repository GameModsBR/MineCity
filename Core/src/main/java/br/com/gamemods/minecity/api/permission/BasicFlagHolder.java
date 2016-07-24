package br.com.gamemods.minecity.api.permission;

import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.world.MinecraftEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * A flag holder that can provide different denial messages per flag and allow users to bypass revoked permissions
 */
public class BasicFlagHolder implements FlagHolder
{
    protected Message defaultMessage = DEFAULT_DENIAL_MESSAGE;
    protected EnumMap<PermissionFlag, DenialEntry> map = new EnumMap<>(PermissionFlag.class);

    public void allow(PermissionFlag flag)
    {
        map.remove(flag);
    }

    public void deny(PermissionFlag flag, Message message)
    {
        map.put(flag, new DenialEntry(message));
    }

    public void deny(PermissionFlag flag)
    {
        map.put(flag, new DenialEntry(defaultMessage));
    }

    public void denyAddBypass(PermissionFlag flag, UUID uuid)
    {
        DenialEntry entry = map.get(flag);
        if(entry == null)
            map.put(flag, entry = new DenialEntry(defaultMessage));

        if(entry.bypasses == null)
            entry.bypasses = new HashSet<>(1);

        entry.bypasses.add(uuid);
    }

    public void denyRemoveBypass(PermissionFlag flag, UUID uuid)
    {
        DenialEntry entry = map.get(flag);
        if(entry == null)
            map.put(flag, entry = new DenialEntry(defaultMessage));

        if(entry.bypasses == null)
            return;

        entry.bypasses.remove(uuid);
        if(entry.bypasses.isEmpty())
            entry.bypasses=null;
    }


    @NotNull
    @Override
    public Optional<Message> can(@NotNull MinecraftEntity entity, @NotNull PermissionFlag action)
    {
        DenialEntry restriction = map.getOrDefault(action, null);
        if( restriction == null
            || action.canBypass && restriction.bypasses != null && restriction.bypasses.contains(entity.getUniqueId())
        )
            return Optional.empty();

        return Optional.of(restriction.message);
    }

    protected static class DenialEntry
    {
        @Nullable
        protected Set<UUID> bypasses;

        @NotNull
        protected Message message;

        public DenialEntry(@NotNull Message message)
        {
            this.message = message;
        }
    }
}
