package br.com.gamemods.minecity.sponge.data.value;

import br.com.gamemods.minecity.api.world.EntityPos;
import br.com.gamemods.minecity.reactive.game.entity.data.EntityData;
import br.com.gamemods.minecity.reactive.game.server.data.ChunkData;
import br.com.gamemods.minecity.sponge.data.manipulator.reactive.SpongeManipulator;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.network.play.server.SPacketUpdateHealth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.data.manipulator.mutable.entity.FoodData;
import org.spongepowered.api.effect.Viewer;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.world.Chunk;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class SpongeEntityData implements EntityData
{
    @Nullable
    private static final Field inventoryItemStacks = Arrays.stream(Container.class.getDeclaredFields())
            .filter(fd-> List.class.isAssignableFrom(fd.getType())).findFirst().orElse(null);

    private final Entity entity;
    private final SpongeManipulator manipulator;

    public SpongeEntityData(SpongeManipulator manipulator, Entity entity)
    {
        this.entity = entity;
        this.manipulator = manipulator;
    }

    @Override
    public Entity getEntity()
    {
        return entity;
    }

    @Override
    public EntityPos getEntityPosition()
    {
        return manipulator.sponge.entityPos(entity);
    }

    @Override
    public boolean sendInventoryUpdate()
    {
        if(!(entity instanceof Player))
            return false;

        if(inventoryItemStacks != null)
            try
            {
                EntityPlayerMP player = (EntityPlayerMP) entity;
                ((List) inventoryItemStacks.get(player.inventoryContainer)).clear();
                player.inventoryContainer.detectAndSendChanges();
                return true;
            }
            catch(Error | Exception e)
            {
                e.printStackTrace();
            }

        return false;
    }

    @Override
    public boolean sendBlockUpdate(int x, int y, int z)
    {
        if(entity instanceof Viewer)
        {
            ((Viewer) entity).sendBlockChange(x, y, z, entity.getWorld().getBlock(x, y, z));
            return true;
        }

        return false;
    }

    @Override
    public boolean sendHungerUpdate()
    {
        if(entity instanceof Player)
        {
            Player player = (Player) entity;
            FoodData data = player.getFoodData();
            int foodLevel = data.foodLevel().get();
            double saturation = data.saturation().get();

            try
            {
                EntityPlayerMP entityPlayer = (EntityPlayerMP) player;
                entityPlayer.connection.sendPacket(new SPacketUpdateHealth(player.health().get().floatValue(), foodLevel, (float) saturation));
            }
            catch(Error | Exception e)
            {
                int diff = foodLevel -1 >= 0? 1 : -1;
                data.foodLevel().set(foodLevel+diff);
                Task.builder().delayTicks(0).execute(()-> data.foodLevel().set(foodLevel-diff)).submit(manipulator.sponge.plugin);
            }
            return true;
        }

        return false;
    }

    @NotNull
    @Override
    public Optional<ChunkData> getChunkData()
    {
        Optional<Chunk> chunk = entity.getWorld().getChunk(entity.getLocation().getChunkPosition());
        if(!chunk.isPresent())
            return Optional.empty();

        return Optional.of(manipulator.server.getChunkData(chunk.get()));
    }

    @Override
    public String toString()
    {
        return "SpongeEntityData{"+
                "entity="+entity+
                '}';
    }
}
