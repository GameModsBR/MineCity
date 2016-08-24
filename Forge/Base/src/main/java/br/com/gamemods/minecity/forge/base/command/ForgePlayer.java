package br.com.gamemods.minecity.forge.base.command;

import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.Server;
import br.com.gamemods.minecity.api.command.CommandSender;
import br.com.gamemods.minecity.api.command.LegacyFormat;
import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.permission.GroupID;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.ChunkPos;
import br.com.gamemods.minecity.api.world.EntityUpdate;
import br.com.gamemods.minecity.api.world.WorldDim;
import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.structure.*;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraftforge.fluids.IFluidBlock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static br.com.gamemods.minecity.api.CollectionUtil.optionalStream;
import static br.com.gamemods.minecity.api.permission.FlagHolder.can;

public abstract class ForgePlayer
        <F extends MineCityForge, P extends EntityPlayer, S extends ForgePlayerSender<P, F>>
        implements IForgePlayer
{
    protected S cmd;
    public ChunkPos lastChunk;
    private int lastX, lastY, lastZ;
    private byte movMessageWait = 0;
    private City lastCity;
    private Plot lastPlot;
    @Nullable
    private Set<GroupID> groups;

    public ForgePlayer(S cmd)
    {
        this.cmd = cmd;
        P player = cmd.sender;
        F mod = cmd.mod;

        lastChunk = new ChunkPos(mod.world(player.worldObj), player.chunkCoordX, player.chunkCoordZ);
        lastX = (int) player.posX;
        lastY = (int) player.posY;
        lastZ = (int) player.posZ;
        Optional<ClaimedChunk> chunk = mod.mineCity.getChunk(lastChunk);
        lastCity = chunk.flatMap(ClaimedChunk::getCity).orElse(null);
        lastPlot = chunk.flatMap(c-> c.getPlotAt(lastX, lastY, lastZ)).orElse(null);
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

    @Override
    public void tick()
    {
        checkStepOnFakeBlock();
        cmd.tick();
        updateGroups();
        checkPosition();
    }

    public void checkStepOnFakeBlock()
    {
        P sender = cmd.sender;
        DisplayedSelection<?> selection = cmd.getSelection();
        F mod = cmd.mod;
        if(selection == null || selection.a == null || selection.display.isEmpty()
                || !selection.world.equals(mod.world(sender.worldObj)))
            return;

        int x = (int) sender.posX;
        int y = (int) sender.posY;
        int z = (int) sender.posZ;
        if(x == lastX && y == lastY && z == lastZ)
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
                    cmd.sendBlock(x, y, z);
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
        P sender = cmd.sender;
        F mod = cmd.mod;

        Optional<Message> message;
        int posY = (int) sender.posY;
        int posZ = (int) sender.posZ;
        int posX = (int) sender.posX;
        if(lastChunk.x != sender.chunkCoordX || lastChunk.z != sender.chunkCoordZ || lastChunk.world.dim != mod.dimension(sender.worldObj))
        {
            ChunkPos chunk = new ChunkPos(mod.world(sender.worldObj), sender.chunkCoordX, sender.chunkCoordZ);
            ClaimedChunk claim = mod.mineCity.getChunk(chunk).orElseGet(()->Inconsistency.claim(chunk));
            City city = claim.getCity().orElse(null);
            Plot plot = null;
            if(city != null)
            {
                plot = claim.getPlotAt(posX, posY, posZ).orElse(null);
                if(city != lastCity)
                {
                    message = optionalStream(
                            can(this, PermissionFlag.ENTER, plot),
                            can(this, PermissionFlag.ENTER, city),
                            can(this, PermissionFlag.LEAVE, lastPlot),
                            can(this, PermissionFlag.LEAVE, lastCity),
                            can(this, PermissionFlag.LEAVE,
                                    lastCity == null? mod.mineCity.nature(lastChunk.world) : null
                            )
                    ).findFirst();

                    if(!message.isPresent())
                    {
                        Message title = new Message("", "${name}", new Object[]{"name", city.getName()});
                        Message subtitle;
                        if(plot != null)
                            subtitle = new Message("","${name}", new Object[]{"name", plot.getName()});
                        else
                            subtitle = null;

                        sendTitle(title, subtitle);
                    }
                }
                else if(plot != lastPlot)
                {
                    message = optionalStream(
                            can(this, PermissionFlag.ENTER, plot),
                            can(this, PermissionFlag.LEAVE, lastPlot)
                    ).findFirst();

                    if(!message.isPresent())
                    {
                        Message subtitle = new Message("","${name}", new Object[]{"name", plot == null? city.getName() : plot.getName()});
                        sendTitle(null, subtitle);
                    }
                }
                else
                    message = Optional.empty();
            }
            else if(lastCity != null)
            {
                message = optionalStream(
                        can(this, PermissionFlag.ENTER, mod.mineCity.nature(chunk.world)),
                        can(this, PermissionFlag.LEAVE, lastPlot),
                        can(this, PermissionFlag.LEAVE, lastCity)
                ).findFirst();

                if(!message.isPresent())
                {
                    Message title = new Message("action.enter.nature", LegacyFormat.GREEN+"Nature");
                    Message subtitle = new Message("","${name}", new Object[]{"name", chunk.world.name()});
                    sendTitle(title, subtitle);
                }
            }
            else if(lastChunk.world.dim != chunk.world.dim)
            {
                message = optionalStream(
                        can(this, PermissionFlag.ENTER, mod.mineCity.nature(chunk.world)),
                        can(this, PermissionFlag.LEAVE, mod.mineCity.nature(lastChunk.world))
                ).findFirst();

                if(!message.isPresent())
                {
                    Message title = new Message("enter.nature", LegacyFormat.GREEN+"Nature");
                    Message subtitle = new Message("","${name}", new Object[]{"name", chunk.world.name()});
                    sendTitle(title, subtitle);
                }
            }
            else
                message = Optional.empty();

            if(!message.isPresent())
            {
                lastCity = city;
                lastChunk = chunk;
                lastPlot = plot;
            }
        }
        else if(posX != lastX || posY != lastY || posZ != lastZ)
        {
            if(lastCity != null)
            {
                Plot plot = mod.mineCity.getChunk(new ChunkPos(mod.world(sender.worldObj), sender.chunkCoordX, sender.chunkCoordZ))
                        .flatMap(chunk -> chunk.getPlotAt(posX, posY, posZ))
                        .orElse(null);

                if(plot != lastPlot)
                {
                    message = optionalStream(
                            can(this, PermissionFlag.ENTER, plot),
                            can(this, PermissionFlag.LEAVE, lastPlot)
                    ).findFirst();

                    if(!message.isPresent())
                    {
                        lastPlot = plot;

                        Message title = new Message("", "${name}", new Object[]{"name", lastCity.getName()});
                        Message subtitle;
                        if(plot != null)
                            subtitle = new Message("","${name}", new Object[]{"name", plot.getName()});
                        else
                            subtitle = null;

                        sendTitle(title, subtitle);
                    }
                }
                else
                    message = Optional.empty();
            }
            else
                message = Optional.empty();
        }
        else
            message = Optional.empty();

        if(message.isPresent())
        {
            if(movMessageWait > 0 && movMessageWait % 5 == 0)
                sender.attackEntityFrom(new DamageSource("sufocation"), 2);

            if(movMessageWait == 0)
            {
                send(new Message("","<msg><red>${msg}</red></msg>", new Object[]{"msg", message.get()}));
                movMessageWait = (byte) 20*3;
            }
            Entity vehicle = mod.vehicle(sender);
            if(vehicle == null)
                cmd.teleport(new BlockPos(lastChunk.world, lastX, lastY, lastZ));
            else
            {
                if(lastChunk.world.dim == mod.dimension(vehicle.worldObj))
                    vehicle.setPosition(lastX+0.5, lastY+0.5, lastZ+0.5);
                else
                    cmd.teleport(new BlockPos(lastChunk.world, lastX, lastY, lastZ));
            }
            return;
        }

        if(movMessageWait > 0)
            movMessageWait--;
        else if((lastX != posX || lastZ != posZ || lastY < posY))
        {
            if(!mod.isTopSolid(sender.worldObj, posX, posY-1, posZ))
            {
                Block block = mod.block(sender.worldObj, posX, posY - 1, posZ);
                if(!(block instanceof BlockLiquid || block instanceof IFluidBlock))
                    return;
            }

            lastX = posX;
            lastY = posY;
            lastZ = posZ;
        }
    }

    public abstract void sendTitle(Message title, Message subtitle);

    @NotNull
    @Override
    public PlayerID identity()
    {
        return cmd.id;
    }

    @Override
    public PlayerID getPlayerId()
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

    @Nullable
    @Override
    public CommandSender getCommandSender()
    {
        return cmd;
    }

    @NotNull
    @Override
    public DisplayedSelection<?> getSelection(WorldDim world)
    {
        return cmd.getSelection(world);
    }

    @Override
    public Server getServer()
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
}
