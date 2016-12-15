package br.com.gamemods.minecity.forge.base.protection.opencomputers;

import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntity;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.command.ForgePlayer;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.ModInterfacesTransformer;
import br.com.gamemods.minecity.reactive.reaction.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;


@Referenced(at = ModInterfacesTransformer.class)
public interface IDrone extends IEntity, IAgent, IEnvironmentHost, IRotatable
{
    @NotNull
    @Override
    default Type getType()
    {
        return Type.UNCLASSIFIED;
    }

    @Nullable
    @Override
    default PermissionFlag getPlayerAttackType()
    {
        return PermissionFlag.MODIFY;
    }

    @Nullable
    @Override
    default UUID getEntityOwnerId()
    {
        return ownerUUID();
    }

    @Override
    default Reaction reactPlayerInteraction(ForgePlayer<?, ?, ?> player, IItemStack stack, boolean offHand)
    {
        MineCityForge mod = player.getServer();
        BlockPos pos = getBlockPos(mod);
        if(player.cmd.sender.isSneaking() && stack != null && OCHooks.isWrench(stack))
        {
            TriggeredReaction reaction;
            if(player.getEntityUUID().equals(ownerUUID()))
                reaction = new ApproveReaction(pos, PermissionFlag.MODIFY);
            else
                reaction = new SingleBlockReaction(pos, PermissionFlag.MODIFY).addDenialListener((reaction1, permissible, flag, pos1, message) ->
                        sendSpawnPackets(player.cmd.sender)
                );

            return reaction.addAllowListener((r, permissible, flag, pos1, message) ->
                    mod.consumeItemsOrAddOwnerIf(getEntityPos(mod), 1, 1, 1, null, player.identity(), drop->
                            drop.getStack().getUnlocalizedName().equals("item.oc.Drone")
                    )
            );
        }
        else if(player.getEntityUUID().equals(ownerUUID()))
        {
            return NoReaction.INSTANCE;
        }

        return new SingleBlockReaction(pos, PermissionFlag.OPEN);
    }

    @Override
    default Reaction reactPlayerAttackDirect(IEntityPlayerMP player, IItemStack stack, boolean offHand)
    {
        if(player.getUniqueID().equals(ownerUUID()))
            return NoReaction.INSTANCE;

        return new SingleBlockReaction(getBlockPos(player.getServer()), PermissionFlag.CLICK);
    }
}
