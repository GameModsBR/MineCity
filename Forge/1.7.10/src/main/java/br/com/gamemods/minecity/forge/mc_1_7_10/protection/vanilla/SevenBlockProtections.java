package br.com.gamemods.minecity.forge.mc_1_7_10.protection.vanilla;

import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.permission.FlagHolder;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.forge.mc_1_7_10.MineCitySeven;
import br.com.gamemods.minecity.forge.mc_1_7_10.command.SevenPlayer;
import br.com.gamemods.minecity.structure.ClaimedChunk;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.world.BlockEvent;

import java.util.Optional;

public class SevenBlockProtections
{
    private final MineCitySeven mod;

    public SevenBlockProtections(MineCitySeven mod)
    {
        this.mod = mod;
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockEvent.PlaceEvent event)
    {
        BlockPos pos = new BlockPos(mod.world(event.world), event.x, event.y, event.z);
        ClaimedChunk chunk = mod.mineCity.provideChunk(pos.getChunk());
        FlagHolder holder = chunk.getFlagHolder(pos);

        SevenPlayer player = mod.player(event.player);
        Optional<Message> denial = holder.can(player, PermissionFlag.MODIFY);
        if(denial.isPresent())
        {
            event.setCanceled(true);
            player.send(FlagHolder.wrapDeny(denial.get()));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockEvent.BreakEvent event)
    {
        BlockPos pos = new BlockPos(mod.world(event.world), event.x, event.y, event.z);
        ClaimedChunk chunk = mod.mineCity.provideChunk(pos.getChunk());
        FlagHolder holder = chunk.getFlagHolder(pos);

        SevenPlayer player = mod.player(event.getPlayer());
        Optional<Message> denial = holder.can(player, PermissionFlag.MODIFY);
        if(denial.isPresent())
        {
            event.setCanceled(true);
            player.send(FlagHolder.wrapDeny(denial.get()));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onBlockMultiPlace(BlockEvent.MultiPlaceEvent event)
    {
        SevenPlayer player = mod.player(event.player);
        BlockPos blockPos = new BlockPos(mod.world(event.world), event.x, event.y, event.z);
        ClaimedChunk chunk = null;
        for(BlockSnapshot state : event.getReplacedBlockSnapshots())
        {
            blockPos = new BlockPos(blockPos, state.x, state.y, state.z);
            chunk = mod.mineCity.provideChunk(blockPos.getChunk(), chunk);
            FlagHolder holder = chunk.getFlagHolder(blockPos);
            Optional<Message> denial = holder.can(player, PermissionFlag.MODIFY);
            if(denial.isPresent())
            {
                event.setCanceled(true);
                player.send(FlagHolder.wrapDeny(denial.get()));
                return;
            }
        }
    }
}
