package br.com.gamemods.minecity.forge.base.accessors.entity;

import br.com.gamemods.minecity.api.permission.Permissible;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.entity.EntityEnderCrystalTransformer;
import br.com.gamemods.minecity.forge.base.protection.reaction.Reaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.SingleBlockReaction;
import net.minecraft.util.DamageSource;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Referenced(at = EntityEnderCrystalTransformer.class)
public interface IEntityEndCrystal extends IEntity
{
    @NotNull
    @Override
    default Type getType()
    {
        return Type.STRUCTURE;
    }

    @Override
    default Reaction reactPlayerAttack(MineCityForge mod, Permissible player, IItemStack stack,
                                       DamageSource source, float amount, List<Permissible> attackers)
    {
        return new SingleBlockReaction(getBlockPos(mod), PermissionFlag.MODIFY);
    }

    @Override
    default Reaction reactPlayerIgnition(MineCityForge mod, Permissible player, IEntity igniter, int seconds,
                                         List<Permissible> attackers)
    {
        return new SingleBlockReaction(getBlockPos(mod), PermissionFlag.MODIFY);
    }
}
