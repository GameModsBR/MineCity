package br.com.gamemods.minecity.forge.mc_1_7_10.protection.protectmyplane;

import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.permission.FlagHolder;
import br.com.gamemods.minecity.api.permission.Permissible;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntity;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.entity.item.IEntityItem;
import br.com.gamemods.minecity.forge.base.protection.reaction.SingleBlockReaction;
import br.com.gamemods.minecity.forge.mc_1_7_10.protection.vanilla.SevenEntityProtections;
import br.com.gamemods.protectmyplane.event.AircraftAttackEvent;
import br.com.gamemods.protectmyplane.event.AircraftDropEvent;
import br.com.gamemods.protectmyplane.event.PlayerPilotAircraftEvent;
import br.com.gamemods.protectmyplane.event.PlayerSpawnVehicleEvent;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class PlaneListener extends SevenEntityProtections
{
    public PlaneListener(MineCityForge forge)
    {
        super(forge);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onAircraftAttack(AircraftAttackEvent event)
    {
        if(event.entity.worldObj.isRemote || event.source.damageType.equals("starve"))
            return;

        List<Permissible> involved = new ArrayList<>(2);
        getAttackers(event.source, involved);
        if(involved.isEmpty())
        {
            event.setCanceled(true);
            return;
        }

        Permissible direct = involved.get(0);
        if(direct instanceof IEntityPlayerMP && direct.identity().getUniqueId().equals(event.ownerId))
            return;

        Optional<Permissible> opt = involved.stream().filter(FILTER_PLAYER).findFirst();
        if(opt.isPresent() && opt.get().identity().uniqueId.equals(event.ownerId))
            return;

        if(onEntityDamage((IEntity) event.entity, event.source, event.amount))
            event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onAircraftDrop(AircraftDropEvent event)
    {
        IEntity entity = (IEntity) event.entity;
        UUID owner = event.ownerId;
        EntityPlayerMP player = (EntityPlayerMP) entity.getIWorld().getPlayerByUUID(owner);
        if(player == null)
        {
            player = mod.server.getIPlayerList().getIPlayers().stream()
                    .filter(p -> p.getUniqueID().equals(owner)).map(EntityPlayerMP.class::cast).findFirst().orElse(null);

            if(player == null)
                return;
        }

        ItemStack stack = new ItemStack(event.item, event.amount, 0);
        if(!player.inventory.addItemStackToInventory(stack))
        {
            IEntityItem item = (IEntityItem) player.dropItem(event.item, event.amount);
            item.allowToPickup(((IEntityPlayerMP) player).identity());
        }

        player.inventoryContainer.detectAndSendChanges();
        event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onPlayerSpawnVehicle(PlayerSpawnVehicleEvent event)
    {
        SingleBlockReaction reaction = new SingleBlockReaction(
                new BlockPos(mod.world(event.entityPlayer.worldObj), event.x, event.y, event.z),
                PermissionFlag.VEHICLE
        );
        IEntityPlayerMP player = (IEntityPlayerMP) event.entityPlayer;
        Optional<Message> denial = reaction.can(mod.mineCity, player);
        if(denial.isPresent())
        {
            player.send(FlagHolder.wrapDeny(denial.get()));
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onPlayerPilotAircraft(PlayerPilotAircraftEvent event)
    {
        if(event.entityPlayer.getUniqueID().equals(event.ownerId))
            return;

        SingleBlockReaction reaction = new SingleBlockReaction(
                ((IEntity) event.aircraft).getBlockPos(mod),
                PermissionFlag.RIDE
        );
        IEntityPlayerMP player = (IEntityPlayerMP) event.entityPlayer;
        Optional<Message> denial = reaction.can(mod.mineCity, player);
        if(denial.isPresent())
        {
            player.send(FlagHolder.wrapDeny(denial.get()));
            event.setCanceled(true);
        }
    }
}
