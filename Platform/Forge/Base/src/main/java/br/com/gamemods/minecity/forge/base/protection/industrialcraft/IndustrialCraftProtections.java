package br.com.gamemods.minecity.forge.base.protection.industrialcraft;

import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.permission.FlagHolder;
import br.com.gamemods.minecity.api.permission.Identity;
import br.com.gamemods.minecity.api.permission.Permissible;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.shape.Cuboid;
import br.com.gamemods.minecity.api.shape.PrecisePoint;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.accessors.block.IState;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntity;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityLivingBase;
import br.com.gamemods.minecity.forge.base.accessors.world.IWorldServer;
import br.com.gamemods.minecity.forge.base.command.ForgePlayer;
import br.com.gamemods.minecity.forge.base.protection.reaction.ShapeBlockReaction;
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

    public boolean onExplosionIC2(IEntity entity, PrecisePoint point, double power, IEntityLivingBase igniter, double rangeLimit, int radiationRange)
    {
        if(entity == null)
            return true;

        double range = Math.max(Math.ceil(rangeLimit), radiationRange);
        List<Permissible> relative = new ArrayList<>(4);
        relative.add(entity);
        addRelativeEntity(entity, relative);
        if(igniter != null)
        {
            relative.add(igniter);
            addRelativeEntity(igniter, relative);
        }
        initPlayers(relative);

        Permissible who = relative.stream().filter(FILTER_PLAYER).findFirst()
                .orElseGet(
                        () -> relative.stream().filter(p -> p.identity().getType() != Identity.Type.ENTITY).findFirst()
                                .orElseGet(() -> relative.stream().findFirst()
                                        .orElse(entity)
                                )
                );

        Optional<Message> denial = new ShapeBlockReaction(mod.world(entity.getIWorld()),
                new Cuboid(point.subtract(range, range, range).toPoint(), point.add(range, range, range).toPoint()),
                PermissionFlag.MODIFY
        ).can(mod.mineCity, who);

        if(denial.isPresent())
        {
            who.send(FlagHolder.wrapDeny(denial.get()));
            return true;
        }

        return false;
    }
}
