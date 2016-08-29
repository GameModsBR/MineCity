package br.com.gamemods.minecity.forge.base.protection.vanilla;

import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.permission.FlagHolder;
import br.com.gamemods.minecity.api.permission.Identity;
import br.com.gamemods.minecity.api.permission.Permissible;
import br.com.gamemods.minecity.api.world.EntityPos;
import br.com.gamemods.minecity.api.world.MinecraftEntity;
import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.accessors.entity.*;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.command.ForgePlayer;
import br.com.gamemods.minecity.forge.base.protection.reaction.NoReaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.Reaction;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

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

    public boolean onFishingHookHitEntity(IEntity entity, EntityProjectile hook)
    {
        Entity forge = hook.getForgeEntity();
        NBTTagCompound nbt = forge.getEntityData();
        boolean destroy = nbt.getBoolean("MineCityDestroy");

        if(onEntityPullEntity(entity, hook, !destroy))
        {
            if(destroy)
                forge.setDead();
            else
                nbt.setBoolean("MineCityDestroy", true);

            return true;
        }

        return false;
    }

    public boolean onFishingHookBringEntity(IEntity entity, EntityProjectile hook)
    {
        return onEntityPullEntity(entity, hook, true);
    }

    public boolean onEntityPullEntity(IEntity pulled, IEntity other, boolean verbose)
    {
        List<Permissible> relative = new ArrayList<>(1);
        relative.add(other);
        addRelativeEntity(other, relative);

        relative.stream().filter(IEntityPlayerMP.class::isInstance).map(IEntityPlayerMP.class::cast).forEach(mod::player);

        Optional<Permissible> optional = relative.stream().filter(
                permissible -> permissible.identity().getType() == Identity.Type.PLAYER
        ).findFirst();

        if(optional.isPresent())
        {
            Permissible player = optional.get();
            Reaction reaction = pulled.reactPlayerPull(mod, player, other, relative);
            Optional<Message> denial = reaction.can(mod.mineCity, player);
            if(verbose && denial.isPresent())
                player.send(FlagHolder.wrapDeny(denial.get()));

            return denial.isPresent();
        }

        return false;
    }

    public boolean onPotionApply(IEntityLivingBase entity, IPotionEffect effect, IEntity potion)
    {
        List<Permissible> relative = new ArrayList<>(1);
        relative.add(potion);
        addRelativeEntity(potion, relative);

        relative.stream().filter(IEntityPlayerMP.class::isInstance).map(IEntityPlayerMP.class::cast).forEach(mod::player);

        Optional<Permissible> optional = relative.stream().filter(
                permissible -> permissible.identity().getType() == Identity.Type.PLAYER
        ).findFirst();

        if(optional.isPresent())
        {
            Permissible player = optional.get();
            Reaction reaction = entity.reactPlayerApplyPotion(mod, player, effect, potion, relative);
            return reaction.can(mod.mineCity, player).isPresent();
        }

        return false;
    }

    public void onEntityEnterChunk(Entity entity, int fromX, int fromZ, int toX, int toZ)
    {
        if(entity.ticksExisted != 0 || !(entity instanceof Projectile))
            return;

        Projectile projectile = (Projectile) entity;
        ProjectileShooter shooter = projectile.getShooter();
        if(shooter == null)
            projectile.detectShooter(mod);
    }

    private void addRelativeEntity(IEntity entity, List<Permissible> list)
    {
        Predicate<Identity<?>> containsId = identity-> list.stream()
                .filter(MinecraftEntity.class::isInstance).map(MinecraftEntity.class::cast)
                .map(MinecraftEntity::getIdentity).anyMatch(identity::equals);

        Predicate<IEntity> add = owner -> {
            if(list.contains(owner))
                return false;

            int index = list.indexOf(owner.getIdentity());
            if(index >= 0)
                list.set(index, owner);
            else
                list.add(owner);

            return true;
        };

        IEntity owner;
        if(entity instanceof Projectile)
        {
            ProjectileShooter shooter = ((Projectile) entity).getShooter();
            if(shooter != null)
            {
                boolean found = false;
                owner = shooter.getEntity();
                if(owner != null)
                {
                    found = true;
                    if(add.test(owner))
                        addRelativeEntity(owner, list);
                }
                else
                {
                    Identity<?> identity = shooter.getIdentity();
                    if(identity != null && !containsId.test(identity))
                        list.add(identity);
                }

                owner = shooter.getIndirectEntity();
                if(owner != null)
                {
                    found = true;
                    if(add.test(owner))
                        addRelativeEntity(owner, list);
                }
                else
                {
                    Identity<?> identity = shooter.getIndirectId();
                    if(identity != null && !containsId.test(identity))
                        list.add(identity);
                }

                if(!found)
                {
                    EntityPos pos = shooter.getPos();
                    Identity<?> identity = mod.mineCity.provideChunk(pos.getChunk())
                            .getFlagHolder(pos.getBlock()).owner();

                    if(!containsId.test(identity))
                        list.add(identity);
                }
            }
        }

        owner = entity.getEntityOwner();
        if(owner != null)
        {
            if(add.test(owner))
                addRelativeEntity(owner, list);
        }
        else
        {
            UUID uuid = entity.getEntityOwnerId();
            if(uuid == null)
                return;

            PlayerID identity = new PlayerID(uuid, "???");
            if(!containsId.test(identity))
                list.add(identity);
        }
    }

    public boolean onEntityDamage(IEntity entity, DamageSource source, float amount)
    {
        List<Permissible> attackers;
        IItemStack stack;

        if(source instanceof EntityDamageSource)
        {
            Entity direct = source.getSourceOfDamage();
            Entity indirect = source.getEntity();

            boolean hasIndirect = indirect != null && indirect != direct;
            attackers = new ArrayList<>(hasIndirect? 2 : 1);
            attackers.add((Permissible) direct);

            if(hasIndirect)
                attackers.add((Permissible) indirect);

            if(direct != null)
                addRelativeEntity((IEntity) direct, attackers);

            if(hasIndirect)
            {
                addRelativeEntity((IEntity) indirect, attackers);
                stack = null;
            }
            else if(direct instanceof IEntityLivingBase)
                stack = ((IEntityLivingBase) direct).getStackInHand(false);
            else
                stack = null;

            attackers.stream().filter(IEntityPlayerMP.class::isInstance).map(IEntityPlayerMP.class::cast)
                    .forEach(mod::player);
        }
        else
        {
            attackers = Collections.emptyList();
            stack = null;
        }

        Optional<Permissible> optionalPlayer = attackers.stream().filter(permissible ->
                permissible instanceof IEntityPlayerMP || permissible instanceof PlayerID
        ).findFirst();

        Reaction reaction = NoReaction.INSTANCE;
        if(optionalPlayer.isPresent())
        {
            Permissible player = optionalPlayer.get();
            if(stack != null)
                reaction = stack.getIItem().reactPlayerAttack(mod, player, stack, entity, source, amount, attackers);

            reaction = reaction.combine(entity.reactPlayerAttack(mod, player, stack, source, amount, attackers));
            Optional<Message> denial = reaction.can(mod.mineCity, player);
            if(denial.isPresent())
                player.send(FlagHolder.wrapDeny(denial.get()));

            Stream.concat(Stream.of(entity), attackers.stream()).filter(IEntity.class::isInstance).map(IEntity.class::cast).forEach(involved->
                    involved.afterPlayerAttack(mod, player, stack, entity, source, amount, attackers, denial.orElse(null))
            );

            return denial.isPresent();
        }

        return false;
    }
}
