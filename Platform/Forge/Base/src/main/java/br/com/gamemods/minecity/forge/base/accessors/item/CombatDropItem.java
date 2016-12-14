package br.com.gamemods.minecity.forge.base.accessors.item;

import br.com.gamemods.minecity.api.permission.Identity;
import br.com.gamemods.minecity.api.permission.Permissible;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntity;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.entity.item.IEntityItem;
import br.com.gamemods.minecity.forge.base.accessors.entity.projectile.ProjectileShooter;
import br.com.gamemods.minecity.forge.base.protection.reaction.BlameOtherInheritedReaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.DoubleBlockReaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.Reaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.SingleBlockReaction;

public interface CombatDropItem extends IItem
{
    default Reaction onPlayerPickupDoCombat(IEntityPlayerMP player, IEntity item, boolean allowNature)
    {
        MineCityForge mod = player.getServer();
        BlockPos pos = player.getBlockPos(mod);
        if(item instanceof IEntityItem)
        {
            IEntityItem entity = (IEntityItem) item;
            ProjectileShooter shooter = entity.getShooter();
            if(shooter != null)
            {
                Permissible responsible = shooter.getResponsible();
                BlockPos shooterPos = shooter.getPos().getBlock();
                if(responsible == null)
                    responsible = mod.mineCity.provideChunk(shooterPos.getChunk()).getFlagHolder(shooterPos).owner();

                if(allowNature && responsible.identity().getType() == Identity.Type.NATURE)
                    return IItem.super.onPlayerPickup(player, item);

                Reaction reaction = new DoubleBlockReaction(PermissionFlag.PVP,
                        shooterPos, pos
                );

                return new BlameOtherInheritedReaction(responsible, reaction).combine(reaction);
            }
        }

        return new SingleBlockReaction(pos, PermissionFlag.PVP);
    }
}
