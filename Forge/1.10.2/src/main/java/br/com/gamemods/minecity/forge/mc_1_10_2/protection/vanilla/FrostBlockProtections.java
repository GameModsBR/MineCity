package br.com.gamemods.minecity.forge.mc_1_10_2.protection.vanilla;

import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.permission.FlagHolder;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.forge.mc_1_10_2.MineCityFrost;
import br.com.gamemods.minecity.forge.mc_1_10_2.command.FrostPlayer;
import br.com.gamemods.minecity.structure.ClaimedChunk;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Optional;

public class FrostBlockProtections
{
    private final MineCityFrost mod;

    public FrostBlockProtections(MineCityFrost mod)
    {
        this.mod = mod;
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockEvent.PlaceEvent event)
    {
        BlockPos pos = mod.block(event.getWorld(), event.getPos());
        ClaimedChunk chunk = mod.mineCity.provideChunk(pos.getChunk());
        FlagHolder holder = chunk.getFlagHolder(pos);

        FrostPlayer player = mod.player(event.getPlayer());
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
        BlockPos pos = mod.block(event.getWorld(), event.getPos());
        ClaimedChunk chunk = mod.mineCity.provideChunk(pos.getChunk());
        FlagHolder holder = chunk.getFlagHolder(pos);

        FrostPlayer player = mod.player(event.getPlayer());
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
        FrostPlayer player = mod.player(event.getPlayer());
        BlockPos blockPos = mod.block(event.getWorld(), event.getPos());
        ClaimedChunk chunk = null;
        for(BlockSnapshot state : event.getReplacedBlockSnapshots())
        {
            blockPos = mod.block(blockPos, state.getPos());
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
