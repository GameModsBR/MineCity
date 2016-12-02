package br.com.gamemods.minecity.permission;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;

public interface Permission extends Serializable
{
    @NotNull
    String getKey();

    final class PermissionKey implements Permission, Comparable<Permission>
    {
        @NotNull
        private final String key;

        public PermissionKey(@NotNull String key)
        {
            this.key = key;
        }

        @NotNull
        @Override
        public String getKey()
        {
            return key;
        }

        @Override
        public boolean equals(@Nullable Object o)
        {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;

            PermissionKey that = (PermissionKey) o;

            return key.equals(that.key);

        }

        @Override
        public int hashCode()
        {
            return key.hashCode();
        }

        @Override
        public int compareTo(@NotNull Permission o)
        {
            return key.compareTo(o.getKey());
        }
    }
}
