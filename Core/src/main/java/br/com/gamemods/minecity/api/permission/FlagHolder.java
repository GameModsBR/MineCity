package br.com.gamemods.minecity.api.permission;

import br.com.gamemods.minecity.api.CollectionUtil;
import br.com.gamemods.minecity.api.command.LegacyFormat;
import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.world.MinecraftEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Represents an object that can check permissions.
 */
public interface FlagHolder
{
    Message DEFAULT_DENIAL_MESSAGE = new Message("action.denied", "You don't have permission to perform this action.");

    /**
     * The owner of this object
     */
    @NotNull
    default Identity<?> owner()
    {
        return ServerAdmins.INSTANCE;
    }

    /**
     * Checks if an entity has a permission.
     * @param entity The entity to be checked
     * @param action The necessary permission
     * @return Empty if the action is allowed or a informative message if it's not.
     */
    @NotNull
    Optional<Message> can(@NotNull MinecraftEntity entity, @NotNull PermissionFlag action);

    /**
     * Checks if an identity has a permission.
     * @param identity The identity to be checked
     * @param action The necessary permission
     * @return Empty if the action is allowed or a informative message if it's not.
     */
    @NotNull
    Optional<Message> can(@NotNull Identity<?> identity, @NotNull PermissionFlag action);

    /**
     * Checks if an object has a permission
     * @param permissible The identity to be checked
     * @param action The necessary permission
     * @return Empty if the action is allowed or a informative message if it's not.
     */
    @NotNull
    default Optional<Message> can(@NotNull Permissible permissible, @NotNull PermissionFlag action)
    {
        if(permissible instanceof MinecraftEntity)
            return can((MinecraftEntity) permissible, action);
        else
            return can(permissible.identity(), action);
    }

    /**
     * Utility method to be used with {@link CollectionUtil#optionalStream(Supplier[])} to simplify checks for multiple permissions.
     * <p>Example:
     * <pre>
     *     Optional&lt;Message> denial = optionalStream(
     *          can(entity, ENTER, plot),
     *          can(entity, ENTER, city),
     *          can(entity, LEAVE, nature)
     *     ).findFirst();
     * </pre>
     * @param entity The entity to be checked
     * @param flag The necessary permission
     * @param holder A permission holder
     * @return A supplier that will invoke {@link #can(MinecraftEntity, PermissionFlag)} when needed
     */
    static Supplier<Optional<Message>> can(MinecraftEntity entity, PermissionFlag flag, FlagHolder holder)
    {
        if(holder == null)
            return Optional::empty;

        return ()-> holder.can(entity, flag);
    }

    /**
     * Utility method to be used with {@link CollectionUtil#optionalStream(Supplier[])} to simplify checks for multiple permissions.
     * <p>Example:
     * <pre>
     *     Optional&lt;Message> denial = optionalStream(
     *          can(identity, ENTER, plot),
     *          can(identity, ENTER, city),
     *          can(identity, LEAVE, nature)
     *     ).findFirst();
     * </pre>
     * @param identity The identity to be checked
     * @param flag The necessary permission
     * @param holder A permission holder
     * @return A supplier that will invoke {@link #can(Identity, PermissionFlag)} when needed
     */
    static Supplier<Optional<Message>> can(Identity<?> identity, PermissionFlag flag, FlagHolder holder)
    {
        if(holder == null)
            return Optional::empty;

        return ()-> holder.can(identity, flag);
    }

    /**
     * Utility method to be used with {@link Stream#concat(Stream, Stream)} to simplify checks for a single permission in multiple holders.
     * <p>Example:
     * <pre>
     *     Optional&lt;Message> denial = Stream.concat(
     *          can(entity, ENTER, plot, city),
     *          can(entity, LEAVE, nature)
     *     ).findFirst();
     *
     *     Optional&lt;Message> alternative = Stream.of(
     *          can(entity, ENTER, plot, city),
     *          can(entity, RIDE, plot, city),
     *          can(entity, LEAVE, nature)
     *     ).flatMap(Function.identity()).findFirst();
     * </pre>
     * @param entity The entity to be checked
     * @param flag The necessary permission in all holders
     * @param holders The permission holders to be checked
     * @return A stream that will invoke {@link #can(MinecraftEntity,PermissionFlag)} when needed
     */
    static Stream<Message> can(MinecraftEntity entity, PermissionFlag flag, FlagHolder... holders)
    {
        return Stream.of(holders)
                .map(h-> can(entity, flag, h))
                .map(Supplier::get)
                .filter(Optional::isPresent)
                .map(Optional::get)
                ;
    }

    /**
     * Utility method to be used with {@link Stream#concat(Stream, Stream)} to simplify checks for a single permission in multiple holders.
     * <p>Example:
     * <pre>
     *     Optional&lt;Message> denial = Stream.concat(
     *          can(identity, ENTER, plot, city),
     *          can(identity, LEAVE, nature)
     *     ).findFirst();
     *
     *     Optional&lt;Message> alternative = Stream.of(
     *          can(identity, ENTER, plot, city),
     *          can(identity, RIDE, plot, city),
     *          can(identity, LEAVE, nature)
     *     ).flatMap(Function.identity()).findFirst();
     * </pre>
     * @param identity The identity to be checked
     * @param flag The necessary permission in all holders
     * @param holders The permission holders to be checked
     * @return A stream that will invoke {@link #can(Identity,PermissionFlag)} when needed
     */
    static Stream<Message> can(Identity<?> identity, PermissionFlag flag, FlagHolder... holders)
    {
        return Stream.of(holders)
                .map(h-> can(identity, flag, h))
                .map(Supplier::get)
                .filter(Optional::isPresent)
                .map(Optional::get)
                ;
    }

    /**
     * Wraps a denial message in a container to be displayed to a player.
     * @param message The denial message
     * @return A formatted message
     */
    static Message wrapDeny(Message message)
    {
        return new Message("", LegacyFormat.RED+"${msg}", new Object[]{"msg", message});
    }
}
