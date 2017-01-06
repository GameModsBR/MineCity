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
    @NotNull
    protected Message defaultMessage = DEFAULT_DENIAL_MESSAGE;

    @NotNull
    protected final Map<PermissionFlag, Message> generalPermissions;

    @NotNull
    protected Map<PermissionFlag, Message> defaultMessages;

    public SimpleFlagHolder()
    {
        generalPermissions = new EnumMap<>(PermissionFlag.class);
        defaultMessages = new EnumMap<>(PermissionFlag.class);
    }

    protected SimpleFlagHolder(@NotNull Map<PermissionFlag, Message> map)
    {
        this.generalPermissions = map;
        this.defaultMessages = new EnumMap<>(PermissionFlag.class);
    }

    @NotNull
    public Map<PermissionFlag, Message> getDefaultMessages()
    {
        return defaultMessages;
    }

    public Message getDefaultMessage(PermissionFlag flag)
    {
        Message message = defaultMessages.get(flag);
        return message != null? message : getDefaultMessage();
    }

    @NotNull
    public Message getDefaultMessage()
    {
        return defaultMessage;
    }

    public void setDefaultMessage(@NotNull Message message)
    {
        Message old = this.defaultMessage;
        defaultMessage = message;

        generalPermissions.entrySet().stream().filter(e-> e.getValue().equals(old)).map(Map.Entry::getKey)
                .forEach(f-> generalPermissions.put(f, message));
    }

    /**
     * Checks if a flag is granted by default
     * @param action The flag to be checked
     * @return The denial message if it's not allowed by default
     */
    public Optional<Message> can(@NotNull PermissionFlag action)
    {
        return Optional.ofNullable(mark(generalPermissions.get(action), action));
    }

    @NotNull
    @Override
    public Optional<Message> can(@NotNull MinecraftEntity entity, @NotNull PermissionFlag action)
    {
        if(owner().equals(entity.identity()))
            return Optional.empty();

        return Optional.ofNullable(mark(generalPermissions.get(action), action));
    }

    @NotNull
    @Override
    public Optional<Message> can(@NotNull Identity<?> identity, @NotNull PermissionFlag action)
    {
        if(owner().equals(identity))
            return Optional.empty();

        return Optional.ofNullable(mark(generalPermissions.get(action), action));
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
     * The current default denial message will be used, if the default message changes later it will be updated.
     * @param flag The flag that will be denied
     */
    public void deny(PermissionFlag flag)
    {
        generalPermissions.put(flag, getDefaultMessage(flag));
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

    /**
     * Changes the default permission for all flags in the map, will not affect direct permissions and restrictions.
     * @param flags The flags that will be denied, the map may not contains {@code null} keys or values
     */
    public void denyAll(Map<PermissionFlag, Message> flags)
    {
        generalPermissions.putAll(flags);
    }

    /**
     * Changes the default permission for all flags in the other flag holder, will not affect direct permissions and restrictions.
     * @param holder The flags that will be denied
     */
    public void denyAll(SimpleFlagHolder holder)
    {
        denyAll(holder.generalPermissions);
    }
}
