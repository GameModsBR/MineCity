package br.com.gamemods.minecity.datasource.api;

import br.com.gamemods.minecity.api.Slow;
import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.permission.ExceptFlagHolder;
import br.com.gamemods.minecity.api.permission.FlagHolder;
import br.com.gamemods.minecity.api.permission.Identity;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.datasource.api.SimpleStorageHolder.SimpleMap;
import br.com.gamemods.minecity.datasource.api.unchecked.UncheckedDataSourceException;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ExceptStoredHolder extends ExceptFlagHolder
{
    protected IExceptPermissionStorage permissionStorage;

    public ExceptStoredHolder()
    {
        super(new SimpleMap());
        SimpleMap map = (SimpleMap) generalPermissions;
        map.holder = this;
        map.permissionStorage = ()-> permissionStorage;
    }

    public ExceptStoredHolder(Message defaultDenialMessage)
    {
        this();

        if(defaultDenialMessage != null)
            super.defaultMessage = defaultDenialMessage;
    }

    @Slow
    protected void loadSimplePermissions() throws DataSourceException
    {
        ((SimpleMap)generalPermissions).backend = permissionStorage.loadSimplePermissions(this);
    }

    @Slow
    protected void loadExceptPermissions() throws DataSourceException
    {
        Map<PermissionFlag, Map<Identity<?>, Optional<Message>>> load = permissionStorage.loadExceptPermissions(this);
        load.forEach((flag, except)-> {
            ExceptMap map = createMap(flag);
            strictPermission.put(flag, map);
            except.forEach((identity, message) -> map.backend.put(identity, message.map(Status::new).orElse(DIRECT_ALLOW)));
        });
    }

    @Slow
    @Override
    public void setDefaultMessage(@NotNull Message message) throws UncheckedDataSourceException
    {
        if(!message.equals(defaultMessage))
            try
            {
                permissionStorage.setDefaultMessage(this, message.equals(FlagHolder.DEFAULT_DENIAL_MESSAGE)? null : message);
            }
            catch(DataSourceException e)
            {
                throw new UncheckedDataSourceException(e);
            }

        super.setDefaultMessage(message);
    }

    @Override
    protected ExceptMap createMap(PermissionFlag flag)
    {
        return new ExceptMap(flag);
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
    public void allow(PermissionFlag flag, Identity<?> identity) throws UncheckedDataSourceException
    {
        super.allow(flag, identity);
    }

    @Slow
    @Override
    public void deny(PermissionFlag flag, Identity<?> identity) throws UncheckedDataSourceException
    {
        super.deny(flag, identity);
    }

    @Slow
    @Override
    public void deny(PermissionFlag flag, Identity<?> identity, Message message) throws UncheckedDataSourceException
    {
        super.deny(flag, identity, message);
    }

    @Slow
    @Override
    public void allowAll(PermissionFlag flag) throws UncheckedDataSourceException
    {
        super.allowAll(flag);
    }

    @Slow
    @Override
    public void denyAll(PermissionFlag flag) throws UncheckedDataSourceException
    {
        super.denyAll(flag);
    }

    @Slow
    @Override
    public void denyAll(PermissionFlag flag, Message message) throws UncheckedDataSourceException
    {
        super.denyAll(flag, message);
    }

    @Slow
    @Override
    public void reset(PermissionFlag flag, Identity<?> identity) throws UncheckedDataSourceException
    {
        super.reset(flag, identity);
    }

    @Slow
    @Override
    public void resetAll(Identity<?> identity) throws UncheckedDataSourceException
    {
        super.resetAll(identity);
    }

    @Override
    public void resetAll(PermissionFlag flag)
    {
        strictPermission.getOrDefault(flag, Collections.emptyMap()).values().removeIf(s-> true);
    }

    protected class ExceptMap extends AbstractMap<Identity<?>, Status>
    {
        private HashMap<Identity<?>, Status> backend = new HashMap<>(1);
        private final PermissionFlag flag;

        public ExceptMap(PermissionFlag flag)
        {
            this.flag = flag;
        }

        @Slow
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

        @Slow
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
            if(isEmpty())
                return Collections.emptySet();

            return new AbstractSet<Entry<Identity<?>, Status>>()
            {
                @NotNull
                @Override
                public Iterator<Entry<Identity<?>, Status>> iterator()
                {
                    return new Iterator<Entry<Identity<?>, Status>>()
                    {
                        Iterator<Entry<Identity<?>, Status>> iter = backend.entrySet().iterator();
                        Entry<Identity<?>, Status> last;

                        @Override
                        public boolean hasNext()
                        {
                            return iter.hasNext();
                        }

                        @Override
                        public Entry<Identity<?>, Status> next()
                        {
                            return last = iter.next();
                        }

                        @Override
                        public void remove()
                        {
                            if(last == null)
                                throw new NoSuchElementException();

                            ExceptMap.this.remove(last.getKey());
                        }
                    };
                }

                @Override
                public int size()
                {
                    return ExceptMap.this.size();
                }
            };
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
