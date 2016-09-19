package br.com.gamemods.minecity.forge.base.protection.industrialcraft;

import br.com.gamemods.minecity.api.permission.Permissible;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.accessors.block.IState;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntity;
import br.com.gamemods.minecity.forge.base.accessors.world.IWorldServer;
import br.com.gamemods.minecity.forge.base.command.ForgePlayer;
import br.com.gamemods.minecity.forge.base.protection.vanilla.EntityProtections;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class IndustrialCraftProtections extends EntityProtections
{
    public IndustrialCraftProtections(MineCityForge mod)
    {
        super(mod);
    }

    public boolean onLaserHitsBlock(IEntity laser, IWorldServer world, int x, int y, int z)
    {
        List<Permissible> involved = new ArrayList<>(2);
        addRelativeEntity(laser, involved);
        Optional<Permissible> opt = involved.stream().filter(FILTER_PLAYER).findFirst();
        if(!opt.isPresent())
            return true;

        BlockPos pos = new BlockPos(mod.world(world), x, y, z);
        IState state = world.getIState(pos);

        ForgePlayer player = mod.player(mod.playerOrFake(opt.get(), laser.getIWorld()));
        return state.getIBlock().reactBlockBreak(player, state, pos).can(mod.mineCity, player).isPresent();
    }
}
