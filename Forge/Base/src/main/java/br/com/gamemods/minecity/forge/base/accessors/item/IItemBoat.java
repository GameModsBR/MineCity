package br.com.gamemods.minecity.forge.base.accessors.item;

import br.com.gamemods.minecity.api.MathUtil;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.shape.PrecisePoint;
import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.accessors.IRayTraceResult;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.entity.vehicle.IVehicle;
import br.com.gamemods.minecity.forge.base.accessors.world.IWorldServer;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.ForgeInterfaceTransformer;
import br.com.gamemods.minecity.forge.base.protection.reaction.NoReaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.Reaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.SingleBlockReaction;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.player.EntityPlayerMP;

@Referenced(at = ForgeInterfaceTransformer.class)
public interface IItemBoat extends IItem
{

    @Override
    default Reaction reactRightClick(IEntityPlayerMP player, IItemStack stack, boolean offHand)
    {
        EntityPlayerMP entity = player.getForgeEntity();
        double x = entity.prevPosX + (entity.posX - entity.prevPosX);
        double y = entity.prevPosY + (entity.posY - entity.prevPosY) + player.getEyeHeight();
        double z = entity.prevPosZ + (entity.posZ - entity.prevPosZ);
        PrecisePoint start = new PrecisePoint(x, y, z);

        MineCityForge mod = player.getMineCityPlayer().getServer();
        float pitch = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch);
        float yaw = entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw);
        float sinYaw = MathUtil.sin.applyAsFloat(-yaw * MathUtil.RADIAN - (float)Math.PI);
        float cosYaw = MathUtil.cos.applyAsFloat(-yaw * MathUtil.RADIAN - (float)Math.PI);
        float sinPitch = MathUtil.sin.applyAsFloat(-pitch * MathUtil.RADIAN);
        float cosPitch = -MathUtil.cos.applyAsFloat(-pitch * MathUtil.RADIAN);
        float dx = sinYaw * cosPitch;
        float dz = cosYaw * cosPitch;
        PrecisePoint end = start.add(dx*5, sinPitch*5, dz*5);

        IWorldServer world = player.getIWorld();
        IRayTraceResult result = world.rayTraceBlocks(start, end, true);

        if(result == null || result.getHitType() != 1)
            return NoReaction.INSTANCE;

        SingleBlockReaction reaction = new SingleBlockReaction(result.getHitBlockPos().toBlock(world.getMineCityWorld()), PermissionFlag.VEHICLE);
        reaction.addAllowListener((reaction1, permissible, flag, pos, message) ->
            mod.addPostSpawnListener(pos.precise(), 2, EntityBoat.class, 2, spawned ->
                    ((IVehicle)spawned).setVehicleOwnerIfAbsent(player.getIdentity())
        ));
        return reaction;
    }
}
