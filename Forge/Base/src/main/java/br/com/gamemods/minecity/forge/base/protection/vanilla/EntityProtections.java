package br.com.gamemods.minecity.forge.base.protection.vanilla;

import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.permission.FlagHolder;
import br.com.gamemods.minecity.api.permission.Permissible;
import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.accessors.entity.IEntity;
import br.com.gamemods.minecity.forge.base.accessors.entity.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.entity.Projectile;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.command.ForgePlayer;
import br.com.gamemods.minecity.forge.base.protection.reaction.NoReaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.Reaction;
import net.minecraft.entity.Entity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;

import java.util.Optional;

public class EntityProtections extends ForgeProtections
{
    public EntityProtections(MineCityForge mod)
    {
        super(mod);
    }

    public boolean onPlayerInteractEntity(IEntityPlayerMP entityPlayer, IEntity target, IItemStack stack, boolean offHand)
    {
        ForgePlayer player = mod.player(entityPlayer);
        Reaction reaction;
        if(stack != null)
            reaction = stack.getIItem().reactInteractEntity(entityPlayer, target, stack, offHand);
        else
            reaction = NoReaction.INSTANCE;

        reaction = reaction.combine(target.reactPlayerInteraction(player, stack, offHand));
        Optional<Message> denial = reaction.can(mod.mineCity, player);
        if(denial.isPresent())
        {
            player.send(FlagHolder.wrapDeny(denial.get()));
            return true;
        }

        return false;
    }

    public void onEntityEnterChunk(Entity entity, int fromX, int fromZ, int toX, int toZ)
    {
        if(entity.ticksExisted != 0 || !(entity instanceof Projectile))
            return;

        ((Projectile) entity).detectShooter(mod);
    }

    public boolean onEntityDamage(IEntity entity, DamageSource source, float amount)
    {
        if(source instanceof EntityDamageSource)
        {
            Permissible player;
            Entity attacker = source.getEntity();
            Entity projectile;
            if(source instanceof EntityDamageSourceIndirect)
            {
                projectile = source.getSourceOfDamage();
            }
            else
                projectile = null;

        }

        return false;
    }
}
