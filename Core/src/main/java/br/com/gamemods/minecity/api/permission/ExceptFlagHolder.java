package br.com.gamemods.minecity.api.permission;

import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.world.MinecraftEntity;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static java.util.Collections.emptyMap;

/**
 * A flag holder that can provide different denial messages per flag and allow users to be directly allowed or denied
 * with custom denial messages only for them
 */
public class ExceptFlagHolder extends SimpleFlagHolder
{
    /**
     * An immutable instance of an approval status
     */
    protected final Status DIRECT_ALLOW = new Status();
    protected EnumMap<PermissionFlag, Map<Identity<?>, Status>> strictPermission = new EnumMap<>(PermissionFlag.class);

    @NotNull
    @Override
    public Optional<Message> can(@NotNull MinecraftEntity entity, @NotNull PermissionFlag action)
    {
        Status status = strictPermission.getOrDefault(action, emptyMap()).get(entity.getIdentity());
        if(status != null)
        {
            if(status.message != null)
                return Optional.of(status.message);

            if(action.canBypass)
                return Optional.empty();
        }

        return Optional.ofNullable(generalPermissions.get(action));
    }

    /**
     * Directly allow a flag for an ID, this will take priority over the default permission  and will override the previous direct permission
     * @param flag The flag that will be denied
     * @param identity The entity or group ID
     */
    public void allow(PermissionFlag flag, Identity<?> identity)
    {
        strictPermission.computeIfAbsent(flag, f-> new HashMap<>(1)).put(identity, DIRECT_ALLOW);
    }

    /**
     * Directly restrict a flag for an ID, this will take priority over the default permission and will override the previous direct permission.
     * The current default denial message will be used, if the default message changes later it will not update.
     * @param flag The flag that will be denied
     * @param identity The entity or group ID
     */
    public void deny(PermissionFlag flag, Identity<?> identity)
    {
        deny(flag, identity, defaultMessage);
    }

    /**
     * Directly restrict a flag for an ID, this will take priority over the default permission  and will override the previous direct permission
     * @param flag The flag that will be denied
     * @param identity The entity or group ID
     * @param message The message that will be displayed only to this ID.
     */
    public void deny(PermissionFlag flag, Identity<?> identity, Message message)
    {
        strictPermission.computeIfAbsent(flag, f-> new HashMap<>(1)).put(identity, new Status(message));
    }

    /**
     * Changes the default permission for a flag and removes all direct restrictions, direct permissions will be kept
     * @param flag The flag that will be allowed
     */
    public void allowAll(PermissionFlag flag)
    {
        generalPermissions.remove(flag);
        strictPermission.getOrDefault(flag, emptyMap()).values().removeIf(Status::isDeny);
    }

    /**
     * Changes the default permission for a flag and removes all direct permissions, direct restrictions will be kept.
     * The current default denial message will be used, if the default message changes later it will not update.
     * @param flag The flag that will be denied
     */
    public void denyAll(PermissionFlag flag)
    {
        denyAll(flag, defaultMessage);
    }

    /**
     * Changes the default permission for a flag and removes all direct permissions, direct restrictions will be kept.
     * @param flag The flag that will be denied
     * @param message The message denial message that will be displayed.
     */
    public void denyAll(PermissionFlag flag, Message message)
    {
        generalPermissions.put(flag, message);
        strictPermission.getOrDefault(flag, emptyMap()).values().removeIf(Status::isAllow);
    }

    /**
     * Hold direct permission status
     */
    protected static class Status
    {
        /**
         * The error message to be displayed if this status is revoking permission, {@code null} otherwise.
         */
        public final Message message;

        /**
         * Creates a denial status
         * @param message The message to be displayed
         */
        public Status(@NotNull Message message)
        {
            this.message = message;
        }

        /**
         * Creates an approval status. It's recommended to use {@link #DIRECT_ALLOW} instead.
         */
        protected Status()
        {
            this.message = null;
        }

        /**
         * A check to be used as method reference
         * @return if {@code message == null}
         */
        public boolean isAllow()
        {
            return message == null;
        }

        /**
         * A check to be used as method reference
         * @return if {@code message != null}
         */
        public boolean isDeny()
        {
            return message != null;
        }
    }
}
