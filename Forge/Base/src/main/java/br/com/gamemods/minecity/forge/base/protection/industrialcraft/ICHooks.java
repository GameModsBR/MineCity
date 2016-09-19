package br.com.gamemods.minecity.forge.base.protection.industrialcraft;

import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.permission.Permissible;
import br.com.gamemods.minecity.api.shape.Point;
import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.accessors.block.IState;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntity;
import br.com.gamemods.minecity.forge.base.accessors.world.IWorldServer;
import br.com.gamemods.minecity.forge.base.command.ForgePlayer;
import br.com.gamemods.minecity.forge.base.core.ModEnv;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.industrialcraft.EntityParticleTransformer;
import br.com.gamemods.minecity.forge.base.protection.vanilla.EntityProtections;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

import java.util.List;
import java.util.Optional;

@Referenced
public class ICHooks
{
    @Referenced(at = EntityParticleTransformer.class)
    public static boolean onEntityBreakBlock(Entity mcEntity, World mcWorld, Point point)
    {
        IEntity entity = (IEntity) mcEntity;
        IWorldServer world = (IWorldServer) mcWorld;

        List<Permissible> relatives = ModEnv.entityProtections.getRelatives(entity);
        MineCityForge mod = ModEnv.entityProtections.mod;
        ForgePlayer player = relatives.stream().filter(EntityProtections.FILTER_PLAYER).findFirst()
                .map(perm -> mod.playerOrFake(perm, world))
                .map(mod::player).orElse(null);

        if(player == null)
            return true;

        IState state = world.getIState(point);
        Optional<Message> denial = state.getIBlock()
                .reactBlockBreak(player, state, point.toBlock(mod.world(world)))
                .can(mod.mineCity, player);

        if(denial.isPresent())
        {
            player.sendProjectileDenial(denial.get());
            return true;
        }

        return false;
    }
}
