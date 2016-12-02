package br.com.gamemods.minecity.sponge;

import br.com.gamemods.minecity.economy.DelayedEconomy;
import br.com.gamemods.minecity.economy.EconomyProvider;
import br.com.gamemods.minecity.permission.DelayedPermission;
import br.com.gamemods.minecity.permission.PermissionProvider;

public class SpongeDelayedProviders
{
    public static final EconomyProvider ECONOMY = mineCity -> new DelayedEconomy(mineCity, SpongeProviders.ECONOMY);
    public static final PermissionProvider PERMISSION = mineCity -> new DelayedPermission(mineCity, SpongeProviders.PERMISSION);

    private SpongeDelayedProviders(){}
}
