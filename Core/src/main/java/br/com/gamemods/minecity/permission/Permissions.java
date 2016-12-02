package br.com.gamemods.minecity.permission;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public enum Permissions implements Permission
{
    BYPASS_NATURE_RESTRICTION_CITY_CREATE
    ;
    private static final Map<String, Permission> commands = new HashMap<>();

    public static Permission command(String commandId)
    {
        return commands.computeIfAbsent(commandId, key-> new PermissionKey("minecity.cmd."+key));
    }

    @NotNull
    private final String key;

    Permissions(@NotNull String key)
    {
        this.key = key;
    }

    Permissions()
    {
        key = "minecity."+name().toLowerCase().replace('_','.');
    }

    @NotNull
    @Override
    public String getKey()
    {
        return key;
    }
}
