package br.com.gamemods.minecity.forge.base.command;

import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.command.LegacyFormat;
import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.permission.FlagHolder;
import br.com.gamemods.minecity.api.permission.GroupID;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.ChunkPos;
import br.com.gamemods.minecity.api.world.EntityUpdate;
import br.com.gamemods.minecity.api.world.MinecraftEntity;
import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntity;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.protection.ForgeMovementListener;
import br.com.gamemods.minecity.forge.base.protection.reaction.Reaction;
import br.com.gamemods.minecity.protection.MovementMonitor;
import br.com.gamemods.minecity.structure.City;
import br.com.gamemods.minecity.structure.DisplayedSelection;
import br.com.gamemods.minecity.structure.Nature;
import br.com.gamemods.minecity.structure.Plot;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Stream;

import static br.com.gamemods.minecity.api.CollectionUtil.optionalStream;
import static br.com.gamemods.minecity.api.permission.FlagHolder.can;
import static br.com.gamemods.minecity.api.permission.PermissionFlag.*;

public class ForgePlayer
        <F extends MineCityForge, P extends IEntityPlayerMP, S extends ForgePlayerSender<P, F>>
        implements MinecraftEntity, ForgeMovementListener<IEntityPlayerMP, F>
{
    public final S cmd;
    protected final F mod;
    public final EntityPlayerMP player;
    private final MovementMonitor<IEntityPlayerMP, F> mov;
    @Nullable
    private Set<GroupID> groups;
    public Set<EntityLiving> leashedEntities = new HashSet<>(1);
    public boolean offHand;
    public boolean disablePickup;
    public boolean disablePickupHarvest;
    private boolean disableProjectDenial;

    public ForgePlayer(S cmd)
    {
        this.cmd = cmd;
        player = cmd.sender.getForgeEntity();
        mod = cmd.mod;
        this.mov = createMonitor();
        mod.runAsynchronously(() ->
        {
            try
            {
                groups = new HashSet<>(mod.mineCity.dataSource.getEntityGroups(cmd.id));
            }
            catch(Exception e)
            {
                mod.logger.error("An error occurred while loading the "+getName()+"'s groups!", e);
                mod.callSyncMethod(()-> kick(new Message("task.player.load.groups.failed",
                        "Oops, an error occurred while loading your groups: ${error}",
                        Message.errorArgs(e)
                )));
            }
        });
    }

    protected MovementMonitor<IEntityPlayerMP, F> createMonitor()
    {
        return new MovementMonitor<>(mod, cmd.sender, cmd.sender.getBlockPos(mod), this);
    }

    public void tick()
    {
        checkStepOnFakeBlock();
        cmd.tick();
        updateGroups();
        checkPosition();
        checkItemUse();
    }

    private void checkItemUse()
    {
        IEntityPlayerMP player = (IEntityPlayerMP) this.player;
        IItemStack itemInUse = player.getActiveItemStack();
        if(itemInUse != null)
        {
            Reaction reaction = itemInUse.getIItem().reactItemUseTick(player, itemInUse, player.getActiveItemUseCount());
            Optional<Message> denial = reaction.can(mod.mineCity, player);
            if(denial.isPresent())
            {
                player.stopUsingItem();
                send(FlagHolder.wrapDeny(denial.get()));
                player.sendResetItemInHand();
            }
        }
    }

    public void checkStepOnFakeBlock()
    {
        EntityPlayerMP sender = player;
        DisplayedSelection<?> selection = cmd.getSelection();
        F mod = cmd.mod;
        if(selection == null || selection.a == null || selection.display.isEmpty()
                || !selection.world.equals(mod.world(sender.worldObj)))
            return;

        int x = (int) sender.posX;
        int y = (int) sender.posY;
        int z = (int) sender.posZ;
        if(x == mov.lastX && y == mov.lastY && z == mov.lastZ)
            return;

        for(int i = 0; i <= 1; i++)
        {
            y--;
            if(mod.isTopSolid(sender.worldObj, x, y, z))
                return;

            for(BlockPos pos : selection.display.keySet())
            {
                if(pos.x == x && pos.y == y && pos.z == z)
                {
                    selection.display.remove(pos);
                    cmd.sender.sendBlock(x, y, z);
                    return;
                }
            }
        }
    }

    public void updateGroups()
    {
        F mod = cmd.mod;
        Queue<EntityUpdate> entityUpdates = mod.mineCity.entityUpdates;
        EntityUpdate update = entityUpdates.peek();
        if(update == null || !update.identity.equals(cmd.id))
            return;

        if(groups == null)
        {
            if(entityUpdates.size() > 1)
                entityUpdates.add(entityUpdates.poll());
            return;
        }

        entityUpdates.poll();
        switch(update.type)
        {
            case GROUP_ADDED:
                groups.add(update.groupId);
                break;

            case GROUP_REMOVED:
                groups.remove(update.groupId);
                break;

            default:
                mod.logger.error("Unsupported update entity type: "+update.type);
        }
    }

    public void checkPosition()
    {
        City lastCity = mov.lastCity;
        Plot lastPlot = mov.lastPlot;
        ChunkPos lastChunk = mov.lastClaim.chunk;
        Optional<Message> message = mov.checkPosition(mod.world(player.worldObj), (int)player.posX, (int)player.posY, (int)player.posZ);
        if(message.isPresent())
        {
            if(mov.messageWait > 0 && mov.messageWait % 5 == 0)
                player.attackEntityFrom(new DamageSource("suffocation"), 2);

            if(mov.messageWait == 0)
            {
                send(new Message("","<msg><red>${msg}</red></msg>", new Object[]{"msg", message.get()}));
                mov.messageWait = (byte) 20*3;
            }

            Entity vehicle = mod.vehicle(player);
            if(vehicle == null)
                cmd.teleport(new BlockPos(lastChunk.world, mov.lastX, mov.lastY, mov.lastZ));
            else
            {
                World world = mod.world(lastChunk.world);
                if(world == null)
                    cmd.teleport(new BlockPos(lastChunk.world, mov.lastX, mov.lastY, mov.lastZ));
                else
                    ((IEntity)vehicle).setPosAndUpdate(mov.lastX+0.5, mov.lastY+0.5, mov.lastZ+0.5);
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
            }
            sendNotification(title, subtitle);
        }
        else if(mov.lastPlot != lastPlot)
        {
            if(mov.lastPlot != null)
            {
                sendNotification(null, Message.string(mov.lastPlot.getName()));
            }
            else
            {
                sendNotification(Message.string(mov.lastCity.getName()), null);
            }
        }
    }

    private void sendNotification(Message title, Message subtitle)
    {
        if(mod.mineCity.useTitles)
            cmd.sender.sendTitle(mod, title, subtitle);
        else if(subtitle == null)
            send(new Message("",LegacyFormat.DARK_GRAY+" ~ "+LegacyFormat.GRAY+"${name}", new Object[]{"name", title}));
        else
            send(new Message("",LegacyFormat.DARK_GRAY+" ~ ${title} :"+LegacyFormat.GRAY+" ${sub}", new Object[][]{
                    {"sub", subtitle},
                    {"title", title}
            }));
    }

    private void removeUnleashedEntities()
    {
        leashedEntities.removeIf(entity -> !entity.getLeashed() || entity.getLeashedToEntity() != player);
    }

    @Override
    public Optional<Message> onCityChange(@NotNull City city, Plot plot)
    {
        removeUnleashedEntities();

        FlagHolder destiny = plot != null? plot : city;

        // Check if can enter the plot and leave the previous location
        FlagHolder lastHolder = mov.lastHolder();
        Stream<Message> stream = optionalStream(
                can(this, ENTER, destiny),
                can(this, LEAVE, lastHolder)
        );

        Entity vehicle = mod.vehicle(player);
        boolean modifying = !leashedEntities.isEmpty();
        if(vehicle != null)
        {
            // Check if can use the ride in that location
            stream = Stream.concat(stream, optionalStream(
                    can(this, RIDE, destiny)
            ));

            if(!modifying)
            {
                // Check if the ride is a community ride
                if(vehicle instanceof EntityTameable)
                    modifying = !((EntityTameable) vehicle).isOwner(player);
                else if(vehicle instanceof EntityLiving)
                    modifying = true;
            }
        }

        // Check if the player can move entities
        if(modifying)
            stream = Stream.concat(stream, can(this, MODIFY, plot, lastHolder instanceof Nature? null : lastHolder));

        return stream.findFirst();
    }

    @Override
    public Optional<Message> onPlotEnter(@NotNull Plot plot)
    {
        removeUnleashedEntities();

        // Check if can enter the plot and leave the previous plot
        Stream<Message> stream = optionalStream(
                can(this, ENTER, plot),
                can(this, LEAVE, mov.lastPlot != null? mov.lastPlot : null)
        );

        Entity vehicle = mod.vehicle(player);
        boolean modifying = !leashedEntities.isEmpty();
        if(vehicle != null)
        {
            // Check if can use the ride in the plot
            stream = Stream.concat(stream, optionalStream(
                    can(this, RIDE, plot)
            ));

            if(!modifying)
            {
                // Check if the ride is a community ride
                if(vehicle instanceof EntityTameable)
                    modifying = !((EntityTameable) vehicle).isOwner(player);
                else if(vehicle instanceof EntityLiving)
                    modifying = true;
            }
        }

        // Check if the player can move entities
        if(modifying)
            stream = Stream.concat(stream, can(this, MODIFY, plot, mov.lastPlot != null? mov.lastPlot : mov.lastCity));

        return stream.findFirst();
    }

    @Override
    public Optional<Message> onPlotLeave(@NotNull City city)
    {
        removeUnleashedEntities();
        // Check if can enter the city and leave the plot
        Stream<Message> stream = optionalStream(
                can(this, ENTER, city),
                can(this, LEAVE, mov.lastPlot)
        );

        Entity vehicle = mod.vehicle(player);
        boolean modifying = !leashedEntities.isEmpty();
        if(vehicle != null)
        {
            // Check if can use the ride in the city
            stream = Stream.concat(stream, optionalStream(
                    can(this, RIDE, city)
            ));

            if(!modifying)
            {
                // Check if the ride is a community ride
                if(vehicle instanceof EntityTameable)
                    modifying = !((EntityTameable) vehicle).isOwner(player);
                else if(vehicle instanceof EntityLiving)
                    modifying = true;
            }
        }

        // Check if the player can move entities
        if(modifying)
            stream = Stream.concat(stream, can(this, MODIFY, city, mov.lastPlot));

        return stream.findFirst();
    }

    @Override
    public Optional<Message> onCityLeave(@NotNull Nature nature)
    {
        removeUnleashedEntities();

        boolean modifying = !leashedEntities.isEmpty();
        if(!modifying)
        {
            Entity vehicle = mod.vehicle(player);
            if(vehicle != null)
            {
                // Check if the ride is a community ride
                if(vehicle instanceof EntityTameable)
                    modifying = !((EntityTameable) vehicle).isOwner(player);
                else if(vehicle instanceof EntityLiving)
                    modifying = true;
            }
        }

        FlagHolder lastHolder = mov.lastHolder();
        return optionalStream(
                can(this, ENTER, nature),
                can(this, LEAVE, lastHolder),
                can(this, MODIFY, modifying? lastHolder : null)
        ).findFirst();
    }

    @Override
    public Optional<Message> onNatureChange(@NotNull Nature nature)
    {
        return optionalStream(
                can(this, ENTER, nature),
                can(this, LEAVE, mov.lastHolder())
        ).findFirst();
    }

    @NotNull
    @Override
    public PlayerID identity()
    {
        return cmd.id;
    }

    @NotNull
    @Override
    public UUID getUniqueId()
    {
        return cmd.id.uniqueId;
    }

    @NotNull
    @Override
    public String getName()
    {
        return cmd.id.getName();
    }

    @NotNull
    @Override
    public S getCommandSender()
    {
        return cmd;
    }

    public F getServer()
    {
        return cmd.mod;
    }

    @NotNull
    @Override
    public Type getType()
    {
        return Type.PLAYER;
    }

    @Override
    public boolean isGroupLoaded()
    {
        return groups != null;
    }

    @NotNull
    @Override
    public Set<GroupID> getGroupIds()
    {
        return groups == null? Collections.emptySet() : groups;
    }

    @Override
    public void send(Message message)
    {
        mod.transformer.send(message, cmd.sender);
    }

    @Override
    public void send(Message[] messages)
    {
        mod.transformer.send(messages, cmd.sender);
    }

    @Override
    public boolean kick(Message message)
    {
        cmd.sender.kick(mod.transformer.toLegacy(message));
        return true;
    }

    public void sendProjectileDenial(Message denial)
    {
        if(disableProjectDenial)
            return;

        send(FlagHolder.wrapDeny(denial));
        mod.callSyncMethodDelayed(()-> disableProjectDenial=false, 5);
        disableProjectDenial = true;
    }
}
