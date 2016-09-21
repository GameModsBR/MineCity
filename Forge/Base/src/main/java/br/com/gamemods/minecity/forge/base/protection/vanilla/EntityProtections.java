package br.com.gamemods.minecity.forge.base.protection.vanilla;

import br.com.gamemods.minecity.MineCity;
import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.permission.FlagHolder;
import br.com.gamemods.minecity.api.permission.Identity;
import br.com.gamemods.minecity.api.permission.Permissible;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.shape.PrecisePoint;
import br.com.gamemods.minecity.api.unchecked.BiIntFunction;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.ChunkPos;
import br.com.gamemods.minecity.api.world.EntityPos;
import br.com.gamemods.minecity.api.world.MinecraftEntity;
import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.accessors.IRayTraceResult;
import br.com.gamemods.minecity.forge.base.accessors.block.IBlockSnapshot;
import br.com.gamemods.minecity.forge.base.accessors.block.IState;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntity;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityLivingBase;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IPotionEffect;
import br.com.gamemods.minecity.forge.base.accessors.entity.item.IEntityItem;
import br.com.gamemods.minecity.forge.base.accessors.entity.item.IEntityXPOrb;
import br.com.gamemods.minecity.forge.base.accessors.entity.item.Pickable;
import br.com.gamemods.minecity.forge.base.accessors.entity.projectile.*;
import br.com.gamemods.minecity.forge.base.accessors.item.IItem;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.accessors.nbt.INBTTagCompound;
import br.com.gamemods.minecity.forge.base.accessors.world.IChunk;
import br.com.gamemods.minecity.forge.base.accessors.world.IChunkCache;
import br.com.gamemods.minecity.forge.base.accessors.world.IExplosion;
import br.com.gamemods.minecity.forge.base.accessors.world.IWorldServer;
import br.com.gamemods.minecity.forge.base.command.ForgePlayer;
import br.com.gamemods.minecity.forge.base.protection.ShooterDamageSource;
import br.com.gamemods.minecity.forge.base.protection.reaction.NoReaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.Reaction;
import br.com.gamemods.minecity.structure.ClaimedChunk;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EntityProtections extends ForgeProtections
{
    public static Predicate<Permissible> FILTER_PLAYER = permissible -> permissible.identity().getType() == Identity.Type.PLAYER;

    public EntityProtections(MineCityForge mod)
    {
        super(mod);
    }

    public void onExplosionDetonate(IWorldServer world, IExplosion explosion, List<IEntity> entities, List<BlockPos> blocks)
    {
        IEntityLivingBase igniter = explosion.getWhoPlaced();
        final IEntity exploder;
        {
            IEntity ex = explosion.getExploder();
            if(ex == null)
            {
                ex = (IEntity) new EntityTNTPrimed((World) world, explosion.getExplosionX(),
                        explosion.getExplosionY(), explosion.getExplosionZ(), (EntityLivingBase) igniter
                );
            }

            exploder = ex;
        }

        if(!entities.isEmpty())
        {
            DamageSource damage = new EntityDamageSourceIndirect("explosion", (Entity) exploder,
                    igniter == null? (Entity) exploder : (Entity) igniter
            ).setExplosion();

            entities.removeIf(entity -> onEntityDamage(entity, damage, 10, true));
        }

        List<Permissible> relative = new ArrayList<>(4);
        relative.add(exploder);
        addRelativeEntity(exploder, relative);
        if(igniter != null)
        {
            relative.add(igniter);
            addRelativeEntity(igniter, relative);
        }
        initPlayers(relative);

        Permissible who = relative.stream().filter(FILTER_PLAYER).findFirst()
                .orElseGet(()-> relative.stream().filter(p-> p.identity().getType() != Identity.Type.ENTITY)
                        .findFirst().orElse(igniter != null? igniter : exploder));


        AtomicReference<ClaimedChunk> last = new AtomicReference<>();
        MineCity mineCity = mod.mineCity;
        blocks.removeIf(pos ->
        {
            ClaimedChunk chunk = mineCity.provideChunk(pos.getChunk(), last.get());
            last.set(chunk);
            return chunk.getFlagHolder(pos).can(who, PermissionFlag.MODIFY).isPresent();
        });
    }

    public boolean onPathFind(PathFinder pathFinder, PathPoint point, IBlockAccess access, EntityLiving entity)
    {
        int width = (int) Math.floor(entity.width+1);
        BiIntFunction<ClaimedChunk> getClaim;
        ClaimedChunk from;
        if(access instanceof IChunkCache)
        {
            IChunkCache cache = (IChunkCache) access;
            from = cache.getClaim(entity.chunkCoordX, entity.chunkCoordZ);
            if(from == null)
                return true;

            getClaim = cache::getClaim;
        }
        else
        {
            IWorldServer world = (IWorldServer) access;
            IChunk chunk = world.getLoadedChunk(entity.chunkCoordX, entity.chunkCoordZ);
            if(chunk == null)
                return true;
            from = chunk.getMineCityClaim();
            if(from == null)
                return true;

            getClaim = (x, z) ->
            {
                IChunk c = world.getLoadedChunk(x, z);
                if(c == null)
                    return null;

                return c.getMineCityClaim();
            };
        }

        Identity<?> fromId = from.getFlagHolder((int) entity.posX, (int) entity.posY, (int) entity.posZ).owner();

        for(int ix = -width; ix <= width; ix++)
            for(int iy = -1; iy <= 1; iy++)
                for(int iz = -width; iz <= width; iz++)
                {
                    ClaimedChunk to = getClaim.apply((point.xCoord + ix) >> 4, (point.zCoord + iz) >> 4);
                    if(to == null)
                        return true;

                    Identity<?> toId = to.getFlagHolder(point.xCoord,point.yCoord,point.zCoord).owner();
                    if(!fromId.equals(toId))
                        return true;
                }

        return false;
    }

    public boolean onPreImpact(IEntity entity, IRayTraceResult traceResult)
    {
        List<Permissible> relative = new ArrayList<>(2);
        relative.add(entity);
        addRelativeEntity(entity, relative);
        initPlayers(relative);
        Permissible who = relative.stream().filter(FILTER_PLAYER).findFirst().orElse(entity);

        Optional<Message> denial = entity.reactImpactPre(mod, traceResult, who, relative).can(mod.mineCity, who);
        if(denial.isPresent())
        {
            who.send(FlagHolder.wrapDeny(denial.get()));
            return true;
        }

        return false;
    }

    public boolean onPostImpact(IEntity entity, IRayTraceResult traceResult, List<IBlockSnapshot> changes)
    {
        if(changes.isEmpty())
            return false;

        List<Permissible> relative = new ArrayList<>(2);
        relative.add(entity);
        addRelativeEntity(entity, relative);
        initPlayers(relative);

        Permissible who = relative.stream().filter(FILTER_PLAYER).findFirst().orElse(entity);

        Reaction reaction = entity.reactImpactPost(mod, traceResult, changes, who, relative);
        Optional<Message> denial = reaction.can(mod.mineCity, who);
        if(denial.isPresent())
        {
            mod.player(who).ifPresent(player -> player.sendProjectileDenial(denial.get()));
            return true;
        }

        return false;
    }

    public boolean onEntityEnterWorld(IEntity entity, BlockPos pos, IEntity spawner)
    {
        if(spawner == null)
            return false;

        List<Permissible> relative = new ArrayList<>(2);
        relative.add(spawner);
        addRelativeEntity(spawner, relative);
        initPlayers(relative);

        Permissible player = relative.stream().filter(FILTER_PLAYER).findFirst().orElse(null);
        if(player != null)
        {
            Reaction reaction = entity.reactPlayerSpawn(mod,player, pos, spawner, relative);
            return reaction.can(mod.mineCity, player).isPresent();
        }

        return false;
    }

    public boolean onEggSpawnChicken(EntityProjectile egg)
    {
        ProjectileShooter shooter = egg.getShooter();
        Permissible cause;
        if(shooter != null)
            cause = shooter.getResponsible(mod.mineCity);
        else
        {
            List<Permissible> involved = new ArrayList<>(2);
            addRelativeEntity(egg, involved);
            initPlayers(involved);
            cause = involved.stream().filter(FILTER_PLAYER).findFirst().orElse(null);
            if(cause == null)
                return true;
        }

        BlockPos pos = egg.getBlockPos(mod);
        return mod.mineCity.provideChunk(pos.getChunk()).getFlagHolder(pos).can(cause, PermissionFlag.PVC).isPresent();
    }

    public void onEntitySpawnByFishingHook(IEntity entity, IEntityFishHook hook)
    {
        IEntityPlayerMP anger = hook.getAnger();
        if(anger != null && entity instanceof Pickable)
        {
            ((Pickable) entity).allowToPickup(anger.identity());
        }
    }

    public void onLivingDropsExp(IEntityLivingBase living, IEntityPlayerMP player, int droppedExp)
    {
        NBTTagCompound nbt = living.getForgeEntity().getEntityData();
        Set<PlayerID> ids = new HashSet<>(2);
        if(player != null)
            ids.add(player.identity());
        if(nbt.hasKey("MineCitySplitXp"))
            ((INBTTagCompound) nbt.getCompoundTag("MineCitySplitXp")).keys().forEach(key-> ids.add(new PlayerID(UUID.fromString(key), nbt.getString(key))));

        AtomicInteger remaining = new AtomicInteger(droppedExp);
        mod.addPostSpawnListener(living.getEntityPos(mod), 1, IEntityXPOrb.class, 2, orb-> {
            ids.forEach(orb::allowToPickup);
            return remaining.addAndGet(-orb.getXp()) > 0;
        });
    }

    public void onLivingDrops(IEntityLivingBase entity, DamageSource source, Collection<EntityItem> drops)
    {
        List<Permissible> attackers = new ArrayList<>(2);
        PlayerID whoDamaged = entity.getWhoDamaged();
        if(whoDamaged != null)
            attackers.add(whoDamaged);
        getAttackers(source, attackers);

        if(!attackers.isEmpty())
        {
            Set<PlayerID> ids = attackers.stream().map(Permissible::identity)
                    .filter(PlayerID.class::isInstance).map(PlayerID.class::cast)
                    .collect(Collectors.toSet());

            drops.stream().map(IEntityItem.class::cast).forEach(item -> ids.forEach(item::allowToPickup));
            NBTTagCompound tag = new NBTTagCompound();
            ids.forEach(id-> tag.setString(id.getUniqueId().toString(), id.getName()));
            entity.getForgeEntity().getEntityData().setTag("MineCitySplitXp", tag);
        }
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
            return false;

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

    public boolean onPlayerPickupItem(IEntityPlayerMP entity, IEntityItem entityItem, boolean silent)
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

            if(!silent)
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

    public void initPlayers(Collection<? extends Permissible> collection)
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

    private boolean shouldAdd(IEntity owner, List<Permissible> list)
    {
        if(list.contains(owner))
            return false;

        int index = list.indexOf(owner.getIdentity());
        if(index >= 0)
            list.set(index, owner);
        else
            list.add(owner);

        return true;
    }

    private boolean containsId(Identity<?> identity, List<Permissible> list)
    {
        return list.stream()
                .filter(MinecraftEntity.class::isInstance).map(MinecraftEntity.class::cast)
                .map(MinecraftEntity::getIdentity).anyMatch(identity::equals);
    }

    public void addRelativeEntity(ProjectileShooter shooter, List<Permissible> list)
    {
        boolean found = false;
        IEntity owner = shooter.getEntity();
        if(owner != null)
        {
            found = true;
            if(shouldAdd(owner, list))
                addRelativeEntity(owner, list);
        }
        else
        {
            Identity<?> identity = shooter.getIdentity();
            if(identity != null && !containsId(identity, list))
                list.add(identity);
        }

        owner = shooter.getIndirectEntity();
        if(owner != null)
        {
            found = true;
            if(shouldAdd(owner, list))
                addRelativeEntity(owner, list);
        }
        else
        {
            Identity<?> identity = shooter.getIndirectId();
            if(identity != null && !containsId(identity, list))
                list.add(identity);
        }

        if(!found)
        {
            EntityPos pos = shooter.getPos();
            Identity<?> identity = mod.mineCity.provideChunk(pos.getChunk())
                    .getFlagHolder(pos.getBlock()).owner();

            if(!containsId(identity, list))
                list.add(identity);
        }
    }

    public void addRelativeEntity(IEntity entity, List<Permissible> list)
    {
        IEntity owner;
        if(entity instanceof Projectile)
        {
            ProjectileShooter shooter = ((Projectile) entity).getShooter();
            if(shooter != null)
                addRelativeEntity(shooter, list);
        }

        owner = entity.getEntityOwner();
        if(owner != null)
        {
            if(shouldAdd(owner, list))
                addRelativeEntity(owner, list);
        }
        else
        {
            UUID uuid = entity.getEntityOwnerId();
            if(uuid == null)
                return;

            PlayerID identity = new PlayerID(uuid, "???");
            if(!containsId(identity, list))
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

    public IItemStack getAttackers(DamageSource source, final List<Permissible> attackers)
    {
        IItemStack stack;

        if(source instanceof EntityDamageSource)
        {
            Entity direct = source.getSourceOfDamage();
            Entity indirect = source.getEntity();

            boolean hasIndirect = indirect != null && indirect != direct;
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
        else if(source instanceof ShooterDamageSource)
        {
            addRelativeEntity(((ShooterDamageSource) source).shooter, attackers);
            initPlayers(attackers);
            return null;
        }
        else
            return null;

        return stack;
    }

    public boolean onPlayerAttack(IEntityPlayerMP player, IEntity target, IItemStack stack, boolean offHand)
    {
        Reaction reaction = NoReaction.INSTANCE;
        if(stack != null)
            reaction = stack.getIItem().reactPlayerAttackDirect(player, target, stack, offHand);

        reaction = reaction.combine(target.reactPlayerAttackDirect(player, stack, offHand));
        Optional<Message> denial = reaction.can(mod.mineCity, player);
        if(denial.isPresent())
        {
            player.send(FlagHolder.wrapDeny(denial.get()));
            return true;
        }

        target.setWhoDamaged(player.identity());
        return false;
    }

    public boolean onEntityDamage(IEntity entity, DamageSource source, float amount, boolean silent)
    {
        List<Permissible> attackers = new ArrayList<>(2);
        IItemStack stack = getAttackers(source, attackers);

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
            {
                if(!silent)
                    player.send(FlagHolder.wrapDeny(denial.get()));
            }
            else
                entity.setWhoDamaged((PlayerID) player.identity());

            Stream.concat(Stream.of(entity), attackers.stream()).filter(IEntity.class::isInstance).map(IEntity.class::cast).forEach(involved->
                    involved.afterPlayerAttack(mod, player, stack, entity, source, amount, attackers, denial.orElse(null))
            );

            return denial.isPresent();
        }

        return false;
    }

    public void onArrowActivate(List<EntityProjectile> list, BlockPos blockPos)
    {
        if(list.isEmpty())
            return;

        FlagHolder holder = mod.mineCity.provideChunk(blockPos.getChunk()).getFlagHolder(blockPos);
        Iterator<EntityProjectile> iterator = list.iterator();
        ArrayList<Permissible> relative = new ArrayList<>(2);
        while(iterator.hasNext())
        {
            relative.clear();

            EntityProjectile arrow = iterator.next();
            addRelativeEntity(arrow, relative);
            initPlayers(relative);

            Optional<Permissible> optionalPlayer = relative.stream().filter(FILTER_PLAYER).findFirst();
            if(optionalPlayer.isPresent())
            {
               if(holder.can(optionalPlayer.get(), PermissionFlag.CLICK).isPresent())
                   iterator.remove();
            }
            else
            {
                iterator.remove();
            }
        }
    }

    public List<Permissible> getRelatives(IEntity entity)
    {
        List<Permissible> list = new ArrayList<>(2);
        addRelativeEntity(entity, list);
        initPlayers(list);
        return list;
    }

    public boolean onEntityTrample(IEntity entity, IWorldServer world, int posX, int posY, int posZ)
    {
        List<Permissible> relatives = getRelatives(entity);
        Optional<Permissible> opt = relatives.stream().filter(FILTER_PLAYER).findFirst();
        if(opt.isPresent())
        {
            Permissible player = opt.get();
            Optional<Message> denial = mod.mineCity.provideChunk(
                    new ChunkPos(mod.world(world), posX >> 4, posZ >> 4)).getFlagHolder(posX, posY, posZ)
                    .can(player, PermissionFlag.MODIFY);

            return denial.isPresent();
        }

        return relatives.stream().filter(IEntity.class::isInstance).map(IEntity.class::cast)
                .anyMatch(en ->
                {
                    if(en.getPlayerAttackType() == PermissionFlag.PVM)
                        return true;

                    BlockPos pos = en.getBlockPos(mod);
                    ClaimedChunk chunk = mod.mineCity.provideChunk(pos.getChunk());
                    Identity<?> from = chunk.getFlagHolder(pos).owner();
                    return mod.mineCity.provideChunk(new ChunkPos(mod.world(world), posX >> 4, posZ >> 4)).getFlagHolder(posX, posY, posZ)
                            .can(from, PermissionFlag.MODIFY).isPresent();
                });
    }
}
