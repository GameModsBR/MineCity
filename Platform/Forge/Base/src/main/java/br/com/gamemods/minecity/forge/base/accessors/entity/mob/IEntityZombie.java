package br.com.gamemods.minecity.forge.base.accessors.entity.mob;

import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.command.ForgePlayer;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.ForgeInterfaceTransformer;
import br.com.gamemods.minecity.forge.base.protection.reaction.NoReaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.Reaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.SingleBlockReaction;
import net.minecraft.entity.monster.EntityZombie;

@Referenced(at = ForgeInterfaceTransformer.class)
public interface IEntityZombie extends IEntityMob
{
    @Override
    default EntityZombie getForgeEntity()
    {
        return (EntityZombie) this;
    }

    default boolean isVillager()
    {
        return getForgeEntity().isVillager();
    }

    @Override
    default Reaction reactPlayerInteractLiving(ForgePlayer<?, ?, ?> player, IItemStack stack, boolean offHand)
    {
        if(stack != null && stack.getMeta() == 0 && isNamed() && isVillager() && stack.getIItem().getUnlocalizedName().equals("item.appleGold"))
            return new SingleBlockReaction(getBlockPos(player.getServer()), PermissionFlag.MODIFY);
        return NoReaction.INSTANCE;
    }
}
