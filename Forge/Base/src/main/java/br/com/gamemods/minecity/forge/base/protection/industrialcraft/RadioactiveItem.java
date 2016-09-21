package br.com.gamemods.minecity.forge.base.protection.industrialcraft;

import br.com.gamemods.minecity.api.permission.Permissible;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntity;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.entity.item.IEntityItem;
import br.com.gamemods.minecity.forge.base.accessors.entity.projectile.ProjectileShooter;
import br.com.gamemods.minecity.forge.base.accessors.item.IItem;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.ModInterfacesTransformer;
import br.com.gamemods.minecity.forge.base.protection.reaction.BlameOtherInheritedReaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.DoubleBlockReaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.Reaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.SingleBlockReaction;

@Referenced(at = ModInterfacesTransformer.class)
public interface RadioactiveItem extends IItem
{
    @Override
    default Reaction onPlayerPickup(IEntityPlayerMP player, IEntity item)
    {
        if(ICHooks.hasCompleteHazmat(player))
            return IItem.super.onPlayerPickup(player, item);

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

                Reaction reaction = new DoubleBlockReaction(PermissionFlag.PVP,
                        shooterPos, pos
                );

                return new BlameOtherInheritedReaction(responsible, reaction).combine(reaction);
            }
        }

        return new SingleBlockReaction(pos, PermissionFlag.PVP);
    }
}
