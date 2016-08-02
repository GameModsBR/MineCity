package br.com.gamemods.minecity.api.permission;

import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.world.MinecraftEntity;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

/**
 * A flag holder that can provide different denial messages per flag
 */
public class SimpleFlagHolder implements FlagHolder
{
    protected Message defaultMessage = DEFAULT_DENIAL_MESSAGE;
    protected final Map<PermissionFlag, Message> generalPermissions;

    public SimpleFlagHolder()
    {
        generalPermissions = new EnumMap<>(PermissionFlag.class);
    }

    protected SimpleFlagHolder(Map<PermissionFlag, Message> map)
    {
        this.generalPermissions = map;
    }

    @NotNull
    @Override
    public Optional<Message> can(@NotNull MinecraftEntity entity, @NotNull PermissionFlag action)
    {
        return Optional.ofNullable(generalPermissions.get(action));
    }

    /**
     * Changes the default permission for a flag, will not affect direct permissions and restrictions
     * @param flag The flag that will be allowed
     */
    public void allow(PermissionFlag flag)
    {
        generalPermissions.remove(flag);
    }

    /**
     * Changes the default permission for a flag, will not affect direct permissions and restrictions.
     * The current default denial message will be used, if the default message changes later it will not update.
     * @param flag The flag that will be denied
     */
    public void deny(PermissionFlag flag)
    {
        generalPermissions.put(flag, defaultMessage);
    }

    /**
     * Changes the default permission for a flag, will not affect direct permissions and restrictions.
     * @param flag The flag that will be denied
     * @param message The message denial message that will be displayed.
     */
    public void deny(PermissionFlag flag, Message message)
    {
        generalPermissions.put(flag, message);
    }
}
