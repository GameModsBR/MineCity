package br.com.gamemods.minecity.forge.base.protection.vehicularmovement;

import br.com.gamemods.minecity.api.MathUtil;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.shape.PrecisePoint;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.accessors.IRayTraceResult;
import br.com.gamemods.minecity.forge.base.accessors.block.IState;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.item.IItem;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.accessors.world.IWorldServer;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.ModInterfacesTransformer;
import br.com.gamemods.minecity.forge.base.protection.reaction.NoReaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.Reaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.SingleBlockReaction;
import net.minecraft.entity.player.EntityPlayerMP;

@Referenced(at = ModInterfacesTransformer.class)
public interface IVehicularItem extends IItem
{
    @Override
    default Reaction reactRightClick(IEntityPlayerMP player, IItemStack stack, boolean offHand)
    {
        EntityPlayerMP entity = (EntityPlayerMP) player;
        double x = entity.prevPosX + (entity.posX - entity.prevPosX);
        double y = entity.prevPosY + (entity.posY - entity.prevPosY) + 1.62 - (double)player.getEyeHeight();
        double z = entity.prevPosZ + (entity.posZ - entity.prevPosZ);
        PrecisePoint start = new PrecisePoint(x, y, z);

        float pitch = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch);
        float yaw = entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw);
        float cosYaw = MathUtil.cos.applyAsFloat((- yaw) * MathUtil.RADIAN - (float)Math.PI);
        float sinYaw = MathUtil.sin.applyAsFloat((- yaw) * MathUtil.RADIAN - (float)Math.PI);
        float cosPitch = - MathUtil.cos.applyAsFloat((- pitch) * MathUtil.RADIAN);
        float sinPitch = MathUtil.sin.applyAsFloat((- pitch) * MathUtil.RADIAN);
        float dx = sinYaw * cosPitch;
        float dz = cosYaw * cosPitch;
        double dist = 5;
        PrecisePoint end = start.add(dx*dist, sinPitch*dist, dz*dist);

        IWorldServer world = player.getIWorld();
        IRayTraceResult result = world.rayTraceBlocks(start, end, true);

        if (result == null || result.getHitType() != 1)
            return NoReaction.INSTANCE;

        MineCityForge mod = player.getServer();
        BlockPos hit = result.getHitBlockPos(mod.world(world));
        IState state = world.getIState(hit);
        if(state != null && state.getIBlock().getUnlocalizedName().equals("tile.snow") && state.getIBlock().getHardness(state, hit) == 0.2F)
            hit = hit.add(Direction.DOWN);

        SingleBlockReaction reaction = new SingleBlockReaction(hit, PermissionFlag.VEHICLE);
        reaction.addAllowListener((reaction1, permissible, flag, pos, message) ->
            mod.addPostSpawnListener(pos.precise(), 2, IVehicularEntity.class, 2, spawned ->
                    spawned.setVehicleOwnerIfAbsent(player.getIdentity())
            )
        );
        return reaction;
    }
}
