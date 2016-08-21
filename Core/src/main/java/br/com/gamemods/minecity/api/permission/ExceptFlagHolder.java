package br.com.gamemods.minecity.api.permission;

import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.world.MinecraftEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    protected final Map<PermissionFlag, Map<Identity<?>, Status>> strictPermission;

    public ExceptFlagHolder()
    {
        strictPermission = new EnumMap<>(PermissionFlag.class);
    }

    protected ExceptFlagHolder(Map<PermissionFlag, Message> general)
    {
        super(general);
        strictPermission = new EnumMap<>(PermissionFlag.class);
    }

    protected ExceptFlagHolder(Map<PermissionFlag, Message> general, Map<PermissionFlag, Map<Identity<?>, Status>> strict)
    {
        super(general);
        strictPermission = strict;
    }

    protected Map<Identity<?>, Status> createMap(PermissionFlag flag)
    {
        return new HashMap<>(1);
    }

    public Map<Identity<?>, Message> getDirectPermissions(PermissionFlag flag)
    {
        Map<Identity<?>, Status> strict = strictPermission.get(flag);
        if(strict == null || strict.isEmpty())
            return Collections.emptyMap();

        Map<Identity<?>, Message> result = new HashMap<>(strict.size());
        strict.forEach((k,v)-> result.put(k, v.message));
        return result;
    }

    @NotNull
    @Override
    public Optional<Message> can(@NotNull Identity<?> identity, @NotNull PermissionFlag action)
    {
        Status status = strictPermission.getOrDefault(action, emptyMap()).get(identity);
        if(status != null)
        {
            if(status.message != null)
                return Optional.of(status.message);

            if(action.canBypass)
                return Optional.empty();
        }

        return Optional.ofNullable(generalPermissions.get(action));
    }

    @Nullable
    protected Status getDirectPermission(@NotNull MinecraftEntity entity, @NotNull PermissionFlag action)
    {
        Map<Identity<?>, Status> perms = strictPermission.get(action);
        if(perms == null || perms.isEmpty())
            return null;
        else
        {
            Status direct = perms.get(entity.getIdentity());
            if(direct == null)
            {
                if(!entity.isGroupLoaded())
                    return new Status(new Message("action.group-loading",
                            "You can't perform this action yet, your permission groups are still loading."
                    ));

                for(GroupID group : entity.getGroupIds())
                {
                    direct = perms.get(group);
                    if(direct != null)
                        return direct;
                }
            }

            return direct;
        }
    }

    @NotNull
    @Override
    public Optional<Message> can(@NotNull MinecraftEntity entity, @NotNull PermissionFlag action)
    {
        Status direct = getDirectPermission(entity, action);
        if(direct != null)
        {
            if(direct.message != null)
                return Optional.of(direct.message);

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
        strictPermission.computeIfAbsent(flag, this::createMap).put(identity, DIRECT_ALLOW);
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
        strictPermission.computeIfAbsent(flag, this::createMap).put(identity, new Status(message));
    }

    /**
     * Removes a direct permission/restriction from an identity.
     * @param flag The flag that will be reset to the default permission
     * @param identity The entity or group ID
     */
    public void reset(PermissionFlag flag, Identity<?> identity)
    {
        strictPermission.getOrDefault(flag, emptyMap()).remove(identity);
    }

    /**
     * Resets all direct permission/restrictions from an identity
     * @param identity The entity or group ID
     */
    public void resetAll(Identity<?> identity)
    {
        strictPermission.values().forEach(v-> v.remove(identity));
    }

    public void resetAll(PermissionFlag flag)
    {
        strictPermission.remove(flag);
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

        @Override
        public boolean equals(Object o)
        {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;

            Status status = (Status) o;

            return message != null? message.equals(status.message) : status.message == null;

        }

        @Override
        public int hashCode()
        {
            return message != null? message.hashCode() : 0;
        }
    }
}
