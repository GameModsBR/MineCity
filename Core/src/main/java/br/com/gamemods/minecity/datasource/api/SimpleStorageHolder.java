package br.com.gamemods.minecity.datasource.api;

import br.com.gamemods.minecity.api.Slow;
import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.permission.SimpleFlagHolder;
import br.com.gamemods.minecity.datasource.api.unchecked.UncheckedDataSourceException;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class SimpleStorageHolder extends SimpleFlagHolder
{
    protected ISimplePermissionStorage permissionStorage;

    public SimpleStorageHolder()
    {
        super(new SimpleMap());
        ((SimpleMap)generalPermissions).holder = this;
    }

    protected void loadPermissions() throws DataSourceException
    {
        ((SimpleMap) generalPermissions).backend = permissionStorage.loadSimplePermissions(this);
    }

    @Slow
    @Override
    public void allow(PermissionFlag flag)
    {
        super.allow(flag);
    }

    @Slow
    @Override
    public void deny(PermissionFlag flag)
    {
        super.deny(flag);
    }

    @Slow
    @Override
    public void deny(PermissionFlag flag, Message message)
    {
        super.deny(flag, message);
    }

    protected static class SimpleMap extends HolderMap<SimpleStorageHolder, Message>
    {
        @Override
        public Message put(PermissionFlag key, Message value) throws UncheckedDataSourceException
        {
            Message current = backend.get(key);
            if(Objects.equals(current, value))
                return backend.put(key, value);

            try
            {
                holder.permissionStorage.deny(holder, key, value == holder.defaultMessage? null : value);
                return backend.put(key, value);
            }
            catch(DataSourceException e)
            {
                throw new UncheckedDataSourceException(e);
            }
        }

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
                holder.permissionStorage.allow(holder, (PermissionFlag) key);
                return backend.remove(key, value);
            }
            catch(DataSourceException e)
            {
                throw new UncheckedDataSourceException(e);
            }
        }

        @Override
        public void clear() throws UncheckedDataSourceException
        {
            if(backend.isEmpty())
                return;

            try
            {
                holder.permissionStorage.allowAll(holder);
            }
            catch(DataSourceException e)
            {
                throw new UncheckedDataSourceException(e);
            }
        }
    }

    protected static abstract class HolderMap<H, V> extends AbstractMap<PermissionFlag, V>
    {
        protected EnumMap<PermissionFlag, V> backend = new EnumMap<>(PermissionFlag.class);
        protected H holder;

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
