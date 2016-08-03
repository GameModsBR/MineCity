package br.com.gamemods.minecity.datasource.api;

import br.com.gamemods.minecity.api.Slow;
import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.permission.SimpleFlagHolder;
import br.com.gamemods.minecity.datasource.api.unchecked.UncheckedDataSourceException;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Supplier;

public class SimpleStorageHolder extends SimpleFlagHolder
{
    protected ISimplePermissionStorage permissionStorage;

    public SimpleStorageHolder()
    {
        super(new SimpleMap());
        SimpleMap map = (SimpleMap) generalPermissions;
        map.holder = this;
        map.permissionStorage = ()-> permissionStorage;
    }

    protected void loadPermissions() throws DataSourceException
    {
        ((SimpleMap) generalPermissions).backend = permissionStorage.loadSimplePermissions(this);
    }

    @Slow
    @Override
    protected void setDefaultMessage(Message message) throws UncheckedDataSourceException
    {
        if(!message.equals(defaultMessage))
            try
            {
                permissionStorage.setDefaultMessage(this, message.equals(DEFAULT_DENIAL_MESSAGE)? null : message);
            }
            catch(DataSourceException e)
            {
                throw new UncheckedDataSourceException(e);
            }

        super.setDefaultMessage(message);
    }

    @Slow
    @Override
    public void allow(PermissionFlag flag) throws UncheckedDataSourceException
    {
        super.allow(flag);
    }

    @Slow
    @Override
    public void deny(PermissionFlag flag) throws UncheckedDataSourceException
    {
        super.deny(flag);
    }

    @Slow
    @Override
    public void deny(PermissionFlag flag, Message message) throws UncheckedDataSourceException
    {
        super.deny(flag, message);
    }

    @Slow
    @Override
    public void denyAll(Map<PermissionFlag, Message> flags)
    {
        super.denyAll(flags);
    }

    protected static class SimpleMap extends HolderMap<Message>
    {
        @Slow
        @Override
        public Message put(PermissionFlag key, Message value) throws UncheckedDataSourceException
        {
            Message current = backend.get(key);
            if(Objects.equals(current, value))
                return backend.put(key, value);

            try
            {
                permissionStorage.get().deny(holder, key, value == holder.getDefaultMessage()? null : value);
                return backend.put(key, value);
            }
            catch(DataSourceException e)
            {
                throw new UncheckedDataSourceException(e);
            }
        }

        @Slow
        @Override
        public void putAll(Map<? extends PermissionFlag, ? extends Message> m) throws UncheckedDataSourceException
        {
            try
            {
                permissionStorage.get().denyAll(holder, m);
                backend.putAll(m);
            }
            catch(DataSourceException e)
            {
                throw new UncheckedDataSourceException(e);
            }
        }

        @Slow
        @Override
        public boolean remove(Object key, Object value) throws UncheckedDataSourceException
        {
            if(!(key instanceof PermissionFlag))
                return false;

            Message current = backend.get(key);
            if(!Objects.equals(current, value))
                return false;

            try
            {
                permissionStorage.get().allow(holder, (PermissionFlag) key);
                return backend.remove(key, value);
            }
            catch(DataSourceException e)
            {
                throw new UncheckedDataSourceException(e);
            }
        }

        @Slow
        @Override
        public Message remove(Object key) throws UncheckedDataSourceException
        {
            if(!(key instanceof PermissionFlag))
                return null;

            if(!backend.containsKey(key))
                return null;

            try
            {
                permissionStorage.get().allow(holder, (PermissionFlag) key);
                return backend.remove(key);
            }
            catch(DataSourceException e)
            {
                throw new UncheckedDataSourceException(e);
            }
        }

        @Slow
        @Override
        public void clear() throws UncheckedDataSourceException
        {
            if(backend.isEmpty())
                return;

            try
            {
                permissionStorage.get().allowAll(holder);
            }
            catch(DataSourceException e)
            {
                throw new UncheckedDataSourceException(e);
            }
        }
    }

    protected static abstract class HolderMap<V> extends AbstractMap<PermissionFlag, V>
    {
        protected EnumMap<PermissionFlag, V> backend = new EnumMap<>(PermissionFlag.class);
        protected SimpleFlagHolder holder;
        protected Supplier<ISimplePermissionStorage> permissionStorage;

        @Override
        public abstract V put(PermissionFlag key, V value)
                throws UncheckedDataSourceException;

        @Override
        public abstract boolean remove(Object key, Object value)
                throws UncheckedDataSourceException;

        @Override
        public abstract void clear()
                throws UncheckedDataSourceException;

        @NotNull
        @Override
        public Set<Entry<PermissionFlag, V>> entrySet()
        {
            return Collections.unmodifiableSet(backend.entrySet());
        }

        @NotNull
        @Override
        public Set<PermissionFlag> keySet()
        {
            return Collections.unmodifiableSet(backend.keySet());
        }

        @NotNull
        @Override
        public Collection<V> values()
        {
            return Collections.unmodifiableCollection(backend.values());
        }

        @Override
        @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
        public boolean equals(Object o)
        {
            return backend.equals(o);
        }

        @Override
        public int hashCode()
        {
            return backend.hashCode();
        }

        @Override
        public V get(Object key)
        {
            return backend.get(key);
        }

        @Override
        public boolean containsKey(Object key)
        {
            return backend.containsKey(key);
        }

        @Override
        public boolean containsValue(Object value)
        {
            return backend.containsValue(value);
        }

        @Override
        public int size()
        {
            return backend.size();
        }
    }
}
