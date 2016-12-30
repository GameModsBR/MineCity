package br.com.gamemods.minecity.sponge.listeners;

import br.com.gamemods.minecity.api.command.LegacyFormat;
import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.permission.FlagHolder;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.ChunkPos;
import br.com.gamemods.minecity.api.world.MinecraftEntity;
import br.com.gamemods.minecity.protection.MovementMonitor;
import br.com.gamemods.minecity.sponge.MineCitySponge;
import br.com.gamemods.minecity.sponge.cmd.PlayerSender;
import br.com.gamemods.minecity.structure.City;
import br.com.gamemods.minecity.structure.DisplayedSelection;
import br.com.gamemods.minecity.structure.Nature;
import br.com.gamemods.minecity.structure.Plot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.entity.TameableData;
import org.spongepowered.api.data.property.AbstractProperty;
import org.spongepowered.api.data.property.block.SolidCubeProperty;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.entity.damage.DamageTypes;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSource;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.*;
import java.util.stream.Stream;

import static br.com.gamemods.minecity.api.CollectionUtil.optionalStream;
import static br.com.gamemods.minecity.api.permission.FlagHolder.can;
import static br.com.gamemods.minecity.api.permission.PermissionFlag.*;

public class PlayerMovement implements CommonMovimentListener<Player>
{
    @NotNull
    private final MovementMonitor<Player, MineCitySponge> mov;
    @NotNull
    private final PlayerSender cmd;
    @NotNull
    private final Player player;
    public Set<Living> leashedEntities = new HashSet<>(1);

    public PlayerMovement(@NotNull PlayerSender sender)
    {
        this.mov = new MovementMonitor<>(sender.server, sender.getHandler(), sender.getPosition().getBlock(), this);
        this.cmd = sender;
        this.player = Objects.requireNonNull(sender.getHandler());
    }

    private void removeUnleashedEntities()
    {
        leashedEntities.removeIf(entity -> !entity
                .get(Keys.LEASH_HOLDER)
                .filter(holder -> holder.getUniqueId().map(player.getUniqueId()::equals).orElse(false))
                .isPresent()
        );
    }

    public void checkStepOnFakeBlock()
    {
        DisplayedSelection<?> selection = cmd.getSelection();
        Location<World> location = player.getLocation();
        World world = location.getExtent();
        if(selection == null || selection.a == null || selection.display.isEmpty()
                || !selection.world.equals(cmd.server.world(world)))
            return;

        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        if(x == mov.lastX && y == mov.lastY && z == mov.lastZ)
            return;

        for(int i = 0; i <= 1; i++)
        {
            y--;
            BlockState block = world.getBlock(x, y, z);
            if(block.getProperty(Direction.UP, SolidCubeProperty.class).map(AbstractProperty::getValue).orElse(false))
                return;

            for(BlockPos pos : selection.display.keySet())
            {
                if(pos.x == x && pos.y == y && pos.z == z)
                {
                    selection.display.remove(pos);
                    player.sendBlockChange(x, y, z, block);
                    return;
                }
            }
        }
    }

    public void checkPosition()
    {
        City lastCity = mov.lastCity;
        Plot lastPlot = mov.lastPlot;
        ChunkPos lastChunk = mov.lastClaim.chunk;
        BlockPos pos = cmd.server.blockPos(player.getLocation());
        Optional<Message> message = mov.checkPosition(pos.world, pos.x, pos.y, pos.z);
        if(message.isPresent())
        {
            if(mov.messageWait > 0 && mov.messageWait % 5 == 0)
                player.damage(2, DamageSource.builder().type(DamageTypes.SUFFOCATE).bypassesArmor().build());

            if(mov.messageWait == 0)
            {
                cmd.send(FlagHolder.wrapDeny(message.get()));
                mov.messageWait = (byte) 20*3;
            }

            Entity vehicle = player.getVehicle().orElse(null);
            if(vehicle == null)
                cmd.teleport(new BlockPos(lastChunk.world, mov.lastX, mov.lastY, mov.lastZ));
            else
            {
                World world = cmd.server.world(lastChunk.world).orElse(null);
                if(world == null)
                    cmd.teleport(new BlockPos(lastChunk.world, mov.lastX, mov.lastY, mov.lastZ));
                else
                    vehicle.setLocation(new Location<>(world, mov.lastX+0.5, mov.lastY+0.5, mov.lastZ+0.5));
            }

            return;
        }

        if(mov.lastCity != lastCity)
        {
            Message title, subtitle;
            if(mov.lastCity != null)
            {
                title = mov.lastCity.getId() > 0? Message.string(mov.lastCity.getName()) : null;
                subtitle = mov.lastPlot != null? Message.string(mov.lastPlot.getName()) : null;
            }
            else
            {
                title = new Message("action.enter.nature", LegacyFormat.GREEN+"Nature");
                subtitle = Message.string(mov.lastClaim.chunk.world.name());
                if(cmd.getAutoClaim())
                {
                    List<String> path = cmd.server.mineCity.commands.find("city.claim");
                    if(path == null)
                        cmd.server.logger.warn("Auto Claim has failed because city.claim command was not found");
                    else
                        cmd.server.mineCity.commands.invoke(cmd, path);
                }
            }
            cmd.sendNotification(title, subtitle);
        }
        else if(mov.lastPlot != lastPlot)
        {
            if(mov.lastPlot != null)
            {
                cmd.sendNotification(null, Message.string(mov.lastPlot.getName()));
            }
            else
            {
                cmd.sendNotification(Message.string(mov.lastCity.getName()), null);
            }
        }

        if(mov.lastCity != null && mov.lastCity != lastCity && mov.lastCity.getPrice() >= 1)
        {
            cmd.send(new Message("action.enter.city-in-sale",
                    "<msg><click><suggest cmd='/city purchase'/><gold>The city ${name} is in sale! Type /city buy to purchase it by ${money}</gold></click></msg>",
                    new Object[][]{
                            {"name", mov.lastCity.getName()},
                            {"money", mov.lastCity.mineCity.economy.format(mov.lastCity.getPrice())}
                    }
            ));
        }

        if(mov.lastPlot != null && mov.lastPlot != lastPlot && mov.lastPlot.getPrice() >= 1)
        {
            cmd.send(new Message("action.enter.plot-in-sale",
                    "<msg><click><suggest cmd='/plot purchase'/><gold>The plot ${name} is in sale! Type /plot buy to purchase it by ${money}</gold></click></msg>",
                    new Object[][]{
                            {"name", mov.lastPlot.getName()},
                            {"money", mov.lastPlot.getCity().mineCity.economy.format(mov.lastPlot.getPrice())}
                    }
            ));
        }
    }

    protected Optional<Boolean> checkVehicle(@NotNull Entity vehicle)
    {
        // Check if the ride is a community ride
        Optional<TameableData> tameable = vehicle.get(TameableData.class);
        if(tameable.isPresent())
            Optional.of(!tameable.get().owner().get().filter(player.getUniqueId()::equals).isPresent());
        else if(vehicle instanceof Living)
            Optional.of(true);
        return Optional.empty();
    }

    @Override
    public Optional<Message> onCityChange(@NotNull City city, @Nullable Plot plot)
    {
        removeUnleashedEntities();
        if(cmd.isAdminMode())
            return Optional.empty();

        FlagHolder destiny = plot != null? plot : city;

        // Check if can enter the plot and leave the previous location
        FlagHolder lastHolder = mov.lastHolder();
        MinecraftEntity self = cmd.getMinecraftEntity();
        Stream<Message> stream = optionalStream(
                can(self, ENTER, destiny),
                can(self, LEAVE, lastHolder)
        );

        Entity vehicle = player.getVehicle().orElse(null);
        boolean modifying = !leashedEntities.isEmpty();
        if(vehicle != null)
        {
            // Check if can use the ride in that location
            stream = Stream.concat(stream, optionalStream(
                    can(self, RIDE, destiny)
            ));

            if(!modifying)
                modifying = checkVehicle(vehicle).orElse(false);
        }

        // Check if the player can move entities
        if(modifying)
            stream = Stream.concat(stream, can(self, MODIFY, plot, lastHolder instanceof Nature ? null : lastHolder));

        return stream.findFirst();
    }

    @Override
    public Optional<Message> onPlotEnter(@NotNull Plot plot)
    {
        removeUnleashedEntities();
        if(cmd.isAdminMode())
            return Optional.empty();

        MinecraftEntity self = cmd.getMinecraftEntity();

        // Check if can enter the plot and leave the previous plot
        Stream<Message> stream = optionalStream(
                can(self, ENTER, plot),
                can(self, LEAVE, mov.lastPlot != null? mov.lastPlot : null)
        );

        Entity vehicle = player.getVehicle().orElse(null);
        boolean modifying = !leashedEntities.isEmpty();
        if(vehicle != null)
        {
            // Check if can use the ride in the plot
            stream = Stream.concat(stream, optionalStream(
                    can(self, RIDE, plot)
            ));

            if(!modifying)
                modifying = checkVehicle(vehicle).orElse(false);
        }

        // Check if the player can move entities
        if(modifying)
            stream = Stream.concat(stream, can(self, MODIFY, plot, mov.lastPlot != null? mov.lastPlot : mov.lastCity));

        return stream.findFirst();
    }

    @Override
    public Optional<Message> onPlotLeave(@NotNull City city)
    {
        removeUnleashedEntities();
        if(cmd.isAdminMode())
            return Optional.empty();

        MinecraftEntity self = cmd.getMinecraftEntity();

        // Check if can enter the city and leave the plot
        Stream<Message> stream = optionalStream(
                can(self, ENTER, city),
                can(self, LEAVE, mov.lastPlot)
        );

        Entity vehicle = player.getVehicle().orElse(null);
        boolean modifying = !leashedEntities.isEmpty();
        if(vehicle != null)
        {
            // Check if can use the ride in the city
            stream = Stream.concat(stream, optionalStream(
                    can(self, RIDE, city)
            ));

            if(!modifying)
                modifying = checkVehicle(vehicle).orElse(false);
        }

        // Check if the player can move entities
        if(modifying)
            stream = Stream.concat(stream, can(self, MODIFY, city, mov.lastPlot));

        return stream.findFirst();
    }

    @Override
    public Optional<Message> onCityLeave(@NotNull Nature nature)
    {
        removeUnleashedEntities();
        if(cmd.isAdminMode())
            return Optional.empty();

        MinecraftEntity self = cmd.getMinecraftEntity();

        boolean modifying = !leashedEntities.isEmpty();
        if(!modifying)
        {
            Entity vehicle = player.getVehicle().orElse(null);
            if(vehicle != null)
                modifying = checkVehicle(vehicle).orElse(false);
        }

        FlagHolder lastHolder = mov.lastHolder();
        return optionalStream(
                can(self, ENTER, nature),
                can(self, LEAVE, lastHolder),
                can(self, MODIFY, modifying? lastHolder : null)
        ).findFirst();
    }

    @Override
    public Optional<Message> onNatureChange(@NotNull Nature nature)
    {
        if(cmd.isAdminMode())
            return Optional.empty();

        MinecraftEntity self = cmd.getMinecraftEntity();

        return optionalStream(
                can(self, ENTER, nature),
                can(self, LEAVE, mov.lastHolder())
        ).findFirst();
    }

    @Override
    public String toString()
    {
        return "PlayerMovement{"+
                "mov="+mov+
                ", player="+player+
                ", leashedEntities="+leashedEntities+
                '}';
    }
}
