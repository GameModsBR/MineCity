package br.com.gamemods.minecity.sponge.data.value;

import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.command.CommandSender;
import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.permission.EntityID;
import br.com.gamemods.minecity.api.permission.Identity;
import br.com.gamemods.minecity.api.world.MinecraftEntity;
import br.com.gamemods.minecity.sponge.MineCitySponge;
import br.com.gamemods.minecity.sponge.cmd.EntitySource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.entity.*;
import org.spongepowered.api.entity.explosive.PrimedTNT;
import org.spongepowered.api.entity.hanging.Hanging;
import org.spongepowered.api.entity.hanging.ItemFrame;
import org.spongepowered.api.entity.living.ArmorStand;
import org.spongepowered.api.entity.living.Hostile;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.projectile.Projectile;
import org.spongepowered.api.entity.vehicle.Boat;
import org.spongepowered.api.entity.vehicle.minecart.ContainerMinecart;
import org.spongepowered.api.entity.vehicle.minecart.FurnaceMinecart;
import org.spongepowered.api.entity.vehicle.minecart.Minecart;
import org.spongepowered.api.entity.vehicle.minecart.RideableMinecart;
import org.spongepowered.api.entity.weather.WeatherEffect;

import java.util.UUID;

public class SpongeMinecraftEntity implements MinecraftEntity
{
    private final MineCitySponge sponge;
    private final Entity entity;
    private final Identity<UUID> identity;
    private final EntitySource sender;

    public SpongeMinecraftEntity(MineCitySponge sponge, Entity entity, EntitySource sender)
    {
        this.sponge = sponge;
        this.entity = entity;
        this.sender = sender;
        if(entity instanceof Player)
            identity = PlayerID.get(entity.getUniqueId(), ((Player) entity).getName());
        else
            identity = new EntityID(type(entity.getType()), entity.getUniqueId(), entity.getTranslation().get());
    }

    @NotNull
    @Override
    public Identity<?> identity()
    {
        return identity;
    }

    @NotNull
    @Override
    public UUID getEntityUUID()
    {
        return identity.uniqueId;
    }

    @Override
    public String getEntityName()
    {
        return identity.getName();
    }

    @NotNull
    @Override
    public Type getType()
    {
        return identity instanceof EntityID? ((EntityID) identity).getEntityType() : Type.PLAYER;
    }

    @Nullable
    @Override
    public CommandSender getCommandSender()
    {
        return sender;
    }

    @Override
    public boolean kick(Message message)
    {
        if(entity instanceof Player)
        {
            ((Player) entity).kick(sponge.transformer.toText(message));
            return true;
        }

        return false;
    }

    public static MinecraftEntity.Type type(EntityType type)
    {
        Class<? extends Entity> entity = type.getEntityClass();
        if(entity.isAssignableFrom(Living.class))
        {
            if(entity.isAssignableFrom(Hostile.class))
                return Type.MONSTER;

            if(entity.isAssignableFrom(ArmorStand.class))
                return Type.STRUCTURE;

            if(entity.isAssignableFrom(Hostile.class))
                return Type.VEHICLE;

            return Type.ANIMAL;
        }

        if(entity.isAssignableFrom(Projectile.class))
            return Type.PROJECTILE;

        if(entity.isAssignableFrom(Hanging.class))
        {
            if(entity.isAssignableFrom(ItemFrame.class))
                return Type.STORAGE;

            return Type.STRUCTURE;
        }

        if(entity.isAssignableFrom(Minecart.class))
        {
            if(entity.isAssignableFrom(RideableMinecart.class))
                return Type.VEHICLE;

            if(entity.isAssignableFrom(ContainerMinecart.class))
                return Type.STORAGE;

            if(entity.isAssignableFrom(FurnaceMinecart.class))
                return Type.STORAGE;

            return Type.STRUCTURE;
        }

        if(entity.isAssignableFrom(EnderCrystal.class))
            return Type.STRUCTURE;

        if(entity.isAssignableFrom(Item.class))
            return Type.ITEM;

        if(entity.isAssignableFrom(ExperienceOrb.class))
            return Type.ITEM;

        if(entity.isAssignableFrom(FallingBlock.class))
            return Type.PROJECTILE;

        if(entity.isAssignableFrom(WeatherEffect.class))
            return Type.PROJECTILE;

        if(entity.isAssignableFrom(Boat.class))
            return Type.VEHICLE;

        if(entity.isAssignableFrom(PrimedTNT.class))
            return Type.PROJECTILE;

        if(entity.isAssignableFrom(AreaEffectCloud.class))
            return Type.PROJECTILE;

        if(entity.isAssignableFrom(ShulkerBullet.class))
            return Type.PROJECTILE;

        return Type.UNCLASSIFIED;
    }
}
