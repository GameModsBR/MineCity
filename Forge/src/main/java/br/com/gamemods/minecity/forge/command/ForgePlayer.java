package br.com.gamemods.minecity.forge.command;

import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.command.CommandSender;
import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.*;
import br.com.gamemods.minecity.forge.MineCityForgeMod;
import br.com.gamemods.minecity.structure.City;
import br.com.gamemods.minecity.structure.ClaimedChunk;
import br.com.gamemods.minecity.structure.Inconsistency;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.MathHelper;
import net.minecraft.world.WorldServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

import static br.com.gamemods.minecity.api.CollectionUtil.optionalStream;
import static br.com.gamemods.minecity.api.permission.FlagHolder.can;

public class ForgePlayer extends ForgeCommandSender<EntityPlayer> implements MinecraftEntity
{
    public ChunkPos lastChunk;
    private int lastX, lastY, lastZ;
    private byte movMessageWait = 0;
    private City lastCity;

    public ForgePlayer(MineCityForgeMod mod, EntityPlayer player)
    {
        super(mod, player);
        lastChunk = new ChunkPos(mod.world(player.worldObj), player.chunkCoordX, player.chunkCoordZ);
        lastX = (int) player.posX;
        lastY = (int) player.posY;
        lastZ = (int) player.posZ;
    }

    public void tick()
    {
        checkPosition();
    }

    public void checkPosition()
    {
        if(lastChunk.x != sender.chunkCoordX || lastChunk.z != sender.chunkCoordZ || lastChunk.world.dim != sender.worldObj.provider.dimensionId)
        {
            ChunkPos chunk = new ChunkPos(mod.world(sender.worldObj), sender.chunkCoordX, sender.chunkCoordZ);
            ClaimedChunk claim = mod.mineCity.getChunk(chunk).orElseGet(()->Inconsistency.claim(chunk));
            City city = claim.getCity().orElse(null);
            if(city != null && city != lastCity)
            {
                Optional<Message> message = optionalStream(
                        can(this, PermissionFlag.ENTER, city),
                        can(this, PermissionFlag.LEAVE, lastCity),
                        can(this, PermissionFlag.LEAVE, mod.mineCity.nature(lastChunk.world))
                        )
                        .findFirst()
                        ;

                if(message.isPresent())
                {
                    if(movMessageWait == 0)
                    {
                        send(message.get());
                        movMessageWait = (byte) 20*3;
                    }
                    teleport(new BlockPos(lastChunk.world, lastX, lastY, lastZ));
                    return;
                }
            }

            lastCity = city;
            lastChunk = chunk;
        }

        if(movMessageWait > 0)
            movMessageWait--;
        else
        {
            lastX = (int) sender.posX;
            lastY = (int) sender.posY;
            lastZ = (int) sender.posZ;
        }
    }

    @Override
    public EntityPos getPosition()
    {
        return new EntityPos(mod.world(sender.worldObj), sender.posX, sender.posY, sender.posZ, sender.rotationPitch, sender.rotationYaw);
    }

    @Override
    public boolean isPlayer()
    {
        return true;
    }

    @Override
    public PlayerID getPlayerId()
    {
        return new PlayerID(sender.getUniqueID(), sender.getCommandSenderName());
    }

    @Override
    public Direction getCardinalDirection()
    {
        return Direction.cardinal8.get(MathHelper.floor_double((double)((sender.rotationYaw + 180.0F) * 8.0F / 360.0F) + 0.5D) & 7);
    }

    @Nullable
    @Override
    public Message teleport(@NotNull BlockPos pos)
    {
        WorldDim current = mod.world(sender.worldObj);
        double x = pos.x+0.5, y = pos.y+0.5, z = pos.z+0.5;
        if(current.equals(pos.world))
        {
            sender.mountEntity(null);
            sender.setPositionAndUpdate(x, y, z);
            return null;
        }

        WorldServer worldServer = mod.world(pos.world);
        if(worldServer == null)
            return new Message("action.teleport.world-not-found",
                    "The destiny world ${name} was not found or is not loaded",
                    new Object[]{"name", pos.world.name()}
            );

        sender.mountEntity(null);
        mod.server.getConfigurationManager().transferPlayerToDimension((EntityPlayerMP) sender, pos.world.dim, worldServer.getDefaultTeleporter());
        sender.setPositionAndUpdate(x, y, z);

        return null;
    }


    @NotNull
    @Override
    public String getName()
    {
        return sender.getCommandSenderName();
    }

    @NotNull
    @Override
    public UUID getUniqueId()
    {
        return sender.getUniqueID();
    }

    @NotNull
    @Override
    public Type getType()
    {
        return Type.PLAYER;
    }

    @Nullable
    @Override
    public CommandSender getCommandSender()
    {
        return this;
    }
}
