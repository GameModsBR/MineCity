package br.com.gamemods.minecity.forge.base.protection.thaumcraft;

import br.com.gamemods.minecity.api.permission.FlagHolder;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.unchecked.BiIntFunction;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.EntityOwnable;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntity;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityLiving;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.command.ForgePlayer;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.ModInterfacesTransformer;
import br.com.gamemods.minecity.forge.base.protection.reaction.*;
import br.com.gamemods.minecity.structure.ClaimedChunk;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.world.IBlockAccess;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Referenced(at = ModInterfacesTransformer.class)
public interface IEntityTravelingTrunk extends IEntityLiving, EntityOwnable
{
    EntityLivingBase getOwnerEntity();
    int getUpgrade();
    int getAnger();
    boolean getStay();
    void setStay(boolean stay);

    @Override
    default Reaction reactPlayerInteractLiving(ForgePlayer<?, ?, ?> fp, IItemStack stack, boolean offHand)
    {
        IEntityPlayerMP player = (IEntityPlayerMP) fp.player;
        if(player.isSneaking())
            return NoReaction.INSTANCE;

        IEntity owner = getOwner();
        TriggeredReaction reaction;
        BlockPos pos = getBlockPos(player.getServer());
        if(player.equals(owner) || getUpgrade() == 3)
            reaction = new ApproveReaction(pos, PermissionFlag.OPEN);
        else
            reaction = new SingleBlockReaction(pos, PermissionFlag.OPEN);

        return reaction.allowToPickup(player, entity-> entity.getStack().getIItem() instanceof IItemTrunkSpawner);
    }

    @Nullable
    @Override
    default PermissionFlag getPlayerAttackType()
    {
        return PermissionFlag.PVP;
    }

    @Override
    default PermissionFlag getPathFinderFlag(BiIntFunction<ClaimedChunk> getClaim, ClaimedChunk from,
                                             FlagHolder fromHolder, PathFinder pathFinder, PathPoint point,
                                             IBlockAccess access)
    {
        return PermissionFlag.ENTER;
    }

    @NotNull
    @Override
    default Type getType()
    {
        return Type.STORAGE;
    }
}
