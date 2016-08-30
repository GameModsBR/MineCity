package br.com.gamemods.minecity.forge.base.protection.vanilla;

import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.permission.FlagHolder;
import br.com.gamemods.minecity.api.permission.Identity;
import br.com.gamemods.minecity.api.permission.Permissible;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.shape.PrecisePoint;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.EntityPos;
import br.com.gamemods.minecity.api.world.MinecraftEntity;
import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.accessors.block.IState;
import br.com.gamemods.minecity.forge.base.accessors.entity.*;
import br.com.gamemods.minecity.forge.base.accessors.item.IItem;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.accessors.world.IWorldServer;
import br.com.gamemods.minecity.forge.base.command.ForgePlayer;
import br.com.gamemods.minecity.forge.base.protection.reaction.NoReaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.Reaction;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class EntityProtections extends ForgeProtections
{
    public static Predicate<Permissible> FILTER_PLAYER = permissible -> permissible.identity().getType() == Identity.Type.PLAYER;

    public EntityProtections(MineCityForge mod)
    {
        super(mod);
    }

    public void onPlayerDrops(IEntityPlayerMP entityPlayer, DamageSource source, Collection<EntityItem> drops)
    {
        PlayerID id = entityPlayer.identity();
        drops.stream().map(IEntityItem.class::cast).forEach(item-> item.allowToPickup(id));
    }

    public boolean onItemToss(IEntityPlayerMP entityPlayer, IEntityItem entityItem)
    {
        ForgePlayer player = mod.player(entityPlayer);

        IItemStack stack = entityItem.getStack();
        Reaction reaction = stack.getIItem().reactItemToss(player, stack, entityItem);
        Optional<Message> denial = reaction.can(mod.mineCity, player);

        if(denial.isPresent())
        {
            player.send(FlagHolder.wrapDeny(denial.get()));
            return true;
        }

        entityItem.allowToPickup(player.identity());
        return false;
    }

    public boolean onXpOrbTargetPlayerEvent(IEntityPlayerMP entityPlayer, IEntityXPOrb entityOrb)
    {
        return onPlayerInteractXpOrb(entityPlayer, entityOrb, true);
    }

    public boolean onPlayerPickupExpEvent(IEntityPlayerMP entityPlayer, IEntityXPOrb entityOrb)
    {
        return onPlayerInteractXpOrb(entityPlayer, entityOrb, false);
    }

    public boolean onPlayerInteractXpOrb(IEntityPlayerMP entityPlayer, IEntityXPOrb entityOrb, boolean silent)
    {
        if(entityOrb.isAllowedToPickup(entityPlayer.identity()))
            return false;

        ForgePlayer player = mod.player(entityPlayer);
        if(player.disablePickup)
            return true;


        BlockPos pos = entityOrb.getBlockPos(mod);
        Optional<Message> denial = mod.mineCity.provideChunk(pos.getChunk())
                .getFlagHolder(pos).can(player,PermissionFlag.PICKUP);

        if(denial.isPresent())
        {
            mod.callSyncMethodDelayed(() -> player.disablePickup = false, 40);
            player.disablePickup = true;
            if(!silent)
                player.send(FlagHolder.wrapDeny(denial.get()));
            return true;
        }

        return false;
    }

    public boolean onPlayerPickupArrowEvent(IEntityPlayerMP entityPlayer, IEntityArrow arrow)
    {
        ForgePlayer player = mod.player(entityPlayer);

        ProjectileShooter shooter = arrow.getShooter();
        if(shooter != null)
        {
            Permissible responsible = shooter.getResponsible();
            if(responsible == null)
            {
                if(player.disablePickup)
                    return true;

                EntityPos pos = shooter.getPos();
                FlagHolder flagHolder = mod.mineCity.provideChunk(pos.getChunk()).getFlagHolder(pos.getBlock());
                Identity<?> owner = flagHolder.owner();
                if(entityPlayer.getIdentity().equals(owner) || !flagHolder.can(entityPlayer, PermissionFlag.PICKUP).isPresent())
                    return false;
            }
            else if(responsible.identity().equals(entityPlayer.identity()))
                return false;
        }

        if(player.disablePickup)
            return true;

        IItemStack stack = arrow.getIArrowStack();
        Reaction react = stack.getIItem().onPlayerPickup(entityPlayer, arrow);
        Optional<Message> denial = react.can(mod.mineCity, entityPlayer);

        if(denial.isPresent())
        {
            mod.callSyncMethodDelayed(() -> player.disablePickup = false, 40);
            player.disablePickup = true;
            player.send(FlagHolder.wrapDeny(denial.get()));
            return true;
        }

        return false;
    }

    public boolean onPlayerPickupItem(IEntityPlayerMP entity, IEntityItem entityItem)
    {
        if(entityItem.isAllowedToPickup(entity.identity()))
            return false;

        IItemStack stack = entityItem.getStack();
        IItem item = stack.getIItem();
        boolean harvest = item.isHarvest(stack);
        ForgePlayer player = mod.player(entity);

        if(harvest)
        {
            if(player.disablePickupHarvest)
                return true;
        }
        else if(player.disablePickup)
            return true;


        Reaction reaction = item.onPlayerPickup(entity, entityItem);
        Optional<Message> denial = reaction.can(mod.mineCity, player);
        if(denial.isPresent())
        {
            if(harvest)
            {
                mod.callSyncMethodDelayed(() -> player.disablePickupHarvest = false, 40);
                player.disablePickupHarvest = true;
            }
            else
            {
                mod.callSyncMethodDelayed(() -> player.disablePickup = false, 40);
                player.disablePickup = true;
            }

            player.send(FlagHolder.wrapDeny(denial.get()));
            return true;
        }

        return false;
    }

    public boolean onPlayerInteractEntityPrecisely(IEntityPlayerMP entityPlayer, IEntity target, IItemStack stack, boolean offHand, PrecisePoint point)
    {
        ForgePlayer player = mod.player(entityPlayer);
        Reaction reaction;
        if(stack != null)
            reaction = stack.getIItem().reactInteractEntityPrecisely(entityPlayer, target, stack, offHand, point);
        else
            reaction = NoReaction.INSTANCE;

        reaction = reaction.combine(target.reactPlayerInteractionPrecise(player, stack, offHand, point));
        Optional<Message> denial = reaction.can(mod.mineCity, player);
        if(denial.isPresent())
        {
            player.send(FlagHolder.wrapDeny(denial.get()));
            return true;
        }

        return false;
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

    private void initPlayers(Collection<? extends Permissible> collection)
    {
        collection.stream().filter(IEntityPlayerMP.class::isInstance).map(IEntityPlayerMP.class::cast).forEach(mod::player);
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

        initPlayers(relative);

        Optional<Permissible> optional = relative.stream().filter(FILTER_PLAYER).findFirst();

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

    public boolean onProjectileModifyBlock(IEntity projectile, IState state, IWorldServer world, int x, int y, int z)
    {
        BlockPos pos = new BlockPos(mod.world(world), x, y, z);
        List<Permissible> relative = new ArrayList<>(1);
        relative.add(projectile);
        addRelativeEntity(projectile, relative);

        initPlayers(relative);

        Optional<Permissible> optional = relative.stream().filter(FILTER_PLAYER).findFirst();

        if(optional.isPresent())
        {
            Permissible player = optional.get();
            Reaction reaction = projectile.reactPlayerModifyWithProjectile(player, projectile, state, world, pos);
            reaction = reaction.combine(
                    state.getIBlock().reactPlayerModifyWithProjectile(player, projectile, state, world, pos)
            );

            return reaction.can(mod.mineCity, player).isPresent();
        }

        return false;
    }

    public boolean onPotionApply(IEntityLivingBase entity, IPotionEffect effect, IEntity potion)
    {
        List<Permissible> relative = new ArrayList<>(1);
        relative.add(potion);
        addRelativeEntity(potion, relative);

        initPlayers(relative);

        Optional<Permissible> optional = relative.stream().filter(FILTER_PLAYER).findFirst();

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

    public boolean onEntityIgniteEntityEvent(IEntity entity, IEntity igniter, int seconds)
    {
        if(entity == igniter || entity.getFireTicks() >= seconds * 20)
            return false;

        List<Permissible> attackers = new ArrayList<>(1);
        attackers.add(igniter);
        addRelativeEntity(igniter, attackers);

        initPlayers(attackers);

        Optional<Permissible> optionalPlayer = attackers.stream().filter(FILTER_PLAYER).findFirst();

        if(optionalPlayer.isPresent())
        {
            Permissible player = optionalPlayer.get();
            Reaction reaction = entity.reactPlayerIgnition(mod, player, igniter, seconds, attackers);
            return reaction.can(mod.mineCity, player).isPresent();
        }

        return false;
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

            initPlayers(attackers);
        }
        else
        {
            attackers = Collections.emptyList();
            stack = null;
        }

        Optional<Permissible> optionalPlayer = attackers.stream().filter(FILTER_PLAYER).findFirst();

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
