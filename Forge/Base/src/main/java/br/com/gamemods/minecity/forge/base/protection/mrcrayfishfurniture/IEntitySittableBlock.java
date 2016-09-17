package br.com.gamemods.minecity.forge.base.protection.mrcrayfishfurniture;

import br.com.gamemods.minecity.api.permission.Permissible;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntity;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.command.ForgePlayer;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.ModInterfacesTransformer;
import br.com.gamemods.minecity.forge.base.protection.reaction.NoReaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.Reaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.SingleBlockReaction;
import net.minecraft.util.DamageSource;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Referenced(at = ModInterfacesTransformer.class)
public interface IEntitySittableBlock extends IEntity
{
    @NotNull
    @Override
    default Type getType()
    {
        return Type.STRUCTURE;
    }

    @Override
    default Reaction reactPlayerInteraction(ForgePlayer<?, ?, ?> player, IItemStack stack, boolean offHand)
    {
        return new SingleBlockReaction(getBlockPos(player.getServer()), PermissionFlag.CLICK);
    }

    @Override
    default Reaction reactPlayerIgnition(MineCityForge mod, Permissible player, IEntity igniter, int seconds,
                                         List<Permissible> attackers)
    {
        return NoReaction.INSTANCE;
    }

    @Override
    default Reaction reactPlayerAttack(MineCityForge mod, Permissible player, IItemStack stack,
                                       DamageSource source, float amount, List<Permissible> attackers)
    {
        return NoReaction.INSTANCE;
    }

    @Override
    default Reaction reactPlayerAttackDirect(IEntityPlayerMP player, IItemStack stack, boolean offHand)
    {
        return NoReaction.INSTANCE;
    }

    @Override
    default Reaction reactPlayerPull(MineCityForge mod, Permissible player, IEntity other,
                                     List<Permissible> relative)
    {
        return NoReaction.INSTANCE;
    }
}
