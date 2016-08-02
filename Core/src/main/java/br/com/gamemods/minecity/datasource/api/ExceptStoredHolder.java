package br.com.gamemods.minecity.datasource.api;

import br.com.gamemods.minecity.api.Slow;
import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.permission.ExceptFlagHolder;
import br.com.gamemods.minecity.api.permission.Identity;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.datasource.api.unchecked.UncheckedDataSourceException;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ExceptStoredHolder extends ExceptFlagHolder
{
    protected IExceptPermissionStorage permissionStorage;

    public ExceptStoredHolder()
    {
        super(new SimpleStorageHolder.SimpleMap());
    }

    protected void loadSimplePermissions() throws DataSourceException
    {
        ((SimpleStorageHolder.SimpleMap)generalPermissions).backend = permissionStorage.loadSimplePermissions(this);
    }

    protected void loadExceptPermissions() throws DataSourceException
    {
        //TODO Implement
        throw new UnsupportedOperationException();
    }

    @Override
    protected Map<Identity<?>, Status> createMap(PermissionFlag flag)
    {
        return new ExceptMap(flag);
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

    @Slow
    @Override
    public void allow(PermissionFlag flag, Identity<?> identity)
    {
        super.allow(flag, identity);
    }

    @Slow
    @Override
    public void deny(PermissionFlag flag, Identity<?> identity)
    {
        super.deny(flag, identity);
    }

    @Slow
    @Override
    public void deny(PermissionFlag flag, Identity<?> identity, Message message)
    {
        super.deny(flag, identity, message);
    }

    @Slow
    @Override
    public void allowAll(PermissionFlag flag)
    {
        super.allowAll(flag);
    }

    @Slow
    @Override
    public void denyAll(PermissionFlag flag)
    {
        super.denyAll(flag);
    }

    @Slow
    @Override
    public void denyAll(PermissionFlag flag, Message message)
    {
        super.denyAll(flag, message);
    }

    protected class ExceptMap extends AbstractMap<Identity<?>, Status>
    {
        private HashMap<Identity<?>, Status> backend = new HashMap<>(1);
        private final PermissionFlag flag;

        public ExceptMap(PermissionFlag flag)
        {
            this.flag = flag;
        }

        public Status put(Identity<?> key, Status value) throws UncheckedDataSourceException
        {
            Status status = backend.get(key);
            if(Objects.equals(status, value))
                return backend.put(key, value);

            try
            {
                permissionStorage.set(ExceptStoredHolder.this, flag, value.isAllow(), key, value.message);
                return backend.put(key, value);
            }
            catch(DataSourceException e)
            {
                throw new UncheckedDataSourceException(e);
            }
        }

        @Override
        public Status remove(Object key) throws UncheckedDataSourceException
        {
            if(!(key instanceof Identity<?>))
                return null;

            if(!backend.containsKey(key))
                return null;

            try
            {
                permissionStorage.remove(ExceptStoredHolder.this, flag, (Identity<?>) key);
                return backend.remove(key);
            }
            catch(DataSourceException e)
            {
                throw new UncheckedDataSourceException(e);
            }
        }

        @NotNull
        @Override
        public Set<Entry<Identity<?>, Status>> entrySet()
        {
            return Collections.unmodifiableSet(backend.entrySet());
        }

        @Override
        public int size()
        {
            return backend.size();
        }

        @Override
        public boolean isEmpty()
        {
            return backend.isEmpty();
        }

        @Override
        public Status get(Object key)
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
    }
}
