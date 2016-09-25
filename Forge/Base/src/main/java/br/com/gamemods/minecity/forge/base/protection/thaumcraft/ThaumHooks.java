package br.com.gamemods.minecity.forge.base.protection.thaumcraft;

import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.forge.base.accessors.IRayTraceResult;
import br.com.gamemods.minecity.forge.base.accessors.block.ITileEntity;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntity;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityLivingBase;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.item.IItem;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.accessors.item.ItemBlockBase;
import br.com.gamemods.minecity.forge.base.accessors.world.IWorldServer;
import br.com.gamemods.minecity.forge.base.core.ModEnv;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.thaumcraft.*;
import br.com.gamemods.minecity.forge.base.protection.ModHooks;
import br.com.gamemods.minecity.forge.base.tile.ITileEntityData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.world.World;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

@Referenced
public class ThaumHooks
{
    public static volatile UUID fireBatSpawner;

    private static Field triggers;
    private static Field configWardedStone;
    private static Method wandGetFocus;
    private static Method isOnCooldown;
    private static Method getArchitectBlocks;
    private static Method focusTradeTrace;
    private static Method getPointedEntity;
    private static Field fireBatOwner;

    public static IEntityPlayerMP getOwner(IEntityFireBat entity)
    {
        try
        {
            if(fireBatOwner == null)
                fireBatOwner = Class.forName("thaumcraft.common.entities.monster.EntityFireBat").getDeclaredField("owner");
            return (IEntityPlayerMP) fireBatOwner.get(entity);
        }
        catch(ReflectiveOperationException e)
        {
            throw new UnsupportedOperationException(e);
        }
    }

    public static IEntity getPointedEntity(IWorldServer world, IEntityPlayerMP player, double range, Class<?> clazz)
    {
        try
        {
            if(getPointedEntity == null)
                getPointedEntity = Class.forName("thaumcraft.common.lib.utils.EntityUtils").getDeclaredMethod("getPointedEntity", World.class, EntityPlayer.class, double.class, Class.class);
            return (IEntity) getPointedEntity.invoke(null, world, player, range, clazz);
        }
        catch(ReflectiveOperationException e)
        {
            throw new UnsupportedOperationException(e);
        }
    }

    public static IRayTraceResult getFocusTradeTraceFromPlayer(IItemFocusTrade trade, IWorldServer world, IEntityPlayerMP player)
    {
        try
        {
            if(focusTradeTrace == null)
            {
                focusTradeTrace = Class.forName("thaumcraft.common.items.wands.foci.ItemFocusTrade").getDeclaredMethod(
                        "getMovingObjectPositionFromPlayer", World.class, EntityPlayer.class
                );
                focusTradeTrace.setAccessible(true);
            }
            return (IRayTraceResult) focusTradeTrace.invoke(trade, world, player);
        }
        catch(ReflectiveOperationException e)
        {
            throw new UnsupportedOperationException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static List<IBlockCoordinates> getArchitectBlocks(IItemFocusWarding item, IItemStack stack, IWorldServer world, int x, int y, int z, int side, IEntityPlayerMP player)
    {
        try
        {
            if(getArchitectBlocks == null)
                getArchitectBlocks = Class.forName("thaumcraft.common.items.wands.foci.ItemFocusWarding").getDeclaredMethod("getArchitectBlocks", ItemStack.class, World.class, int.class, int.class, int.class, int.class, EntityPlayer.class);

            return (List) getArchitectBlocks.invoke(item, stack, world, x, y, z, side, player);
        }
        catch(ReflectiveOperationException e)
        {
            throw new UnsupportedOperationException(e);
        }
    }

    public static boolean isOnWandCooldown(IEntityLivingBase player)
    {
        try
        {
            if(isOnCooldown == null)
            {
                isOnCooldown = Class.forName("thaumcraft.common.items.wands.WandManager").getDeclaredMethod("isOnCooldown", EntityLivingBase.class);
                isOnCooldown.setAccessible(true);
            }

            return (boolean) isOnCooldown.invoke(null, player);
        }
        catch(ReflectiveOperationException e)
        {
            throw new UnsupportedOperationException(e);
        }
    }

    public static IItemFocusBasic getFocus(IItemWandCasting wand, IItemStack stack)
    {
        try
        {
            if(wandGetFocus == null)
                wandGetFocus = Class.forName("thaumcraft.common.items.wands.ItemWandCasting").getDeclaredMethod("getFocus", ItemStack.class);

            return (IItemFocusBasic) wandGetFocus.invoke(wand, stack);
        }
        catch(ReflectiveOperationException e)
        {
            throw new UnsupportedOperationException(e);
        }
    }

    public static boolean getConfigWardedStone()
    {
        try
        {
            if(configWardedStone == null)
                configWardedStone = Class.forName("thaumcraft.common.config.Config").getDeclaredField("wardedStone");
            return configWardedStone.getBoolean(null);
        }
        catch(ReflectiveOperationException e)
        {
            throw new UnsupportedOperationException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static Map<String, HashMap<List, List>> getTriggers()
    {
        try
        {
            if(triggers == null)
            {
                triggers = Class.forName("thaumcraft.api.wands.WandTriggerRegistry").getDeclaredField("triggers");
                triggers.setAccessible(true);
            }

            return (Map) triggers.get(null);
        }
        catch(ReflectiveOperationException e)
        {
            throw new UnsupportedOperationException(e);
        }
    }

    @Referenced(at = TileNodeTransformer.class)
    public static boolean onNodeBreak(TileEntity mcTile, World mcWorld, int x, int y, int z)
    {
        ITileEntity tile = (ITileEntity) mcTile;
        return ModHooks.onBlockAccessOther(
                mcWorld, x, y, z,
                tile.getPosX(), tile.getPosY(), tile.getPosZ(),
                PermissionFlag.MODIFY
        ).isPresent();
    }

    @Referenced(at = BlockAiryTransformer.class)
    public static boolean onAiryApplyPotion(Entity mcEntity, World mcWorld, int x, int y, int z)
    {
        return onAiryDamage(mcEntity, DamageSource.magic, 1, mcWorld, x, y, z);
    }

    @Referenced(at = BlockAiryTransformer.class)
    public static boolean onAiryDamage(Entity mcEntity, DamageSource source, float amount, World mcWorld, int x, int y, int z)
    {
        if(mcWorld.isRemote)
            return false;

        IWorldServer world = (IWorldServer) mcWorld;
        ITileEntity tile = world.getTileEntity(x, y, z);
        PlayerID owner;
        if(tile instanceof ITileNode)
        {
            owner = ((ITileNode) tile).getOwner();
        }
        else if(tile instanceof ITileEntityData)
        {
            owner = ((ITileEntityData) tile).getOwner();
        }
        else
        {
            owner = null;
        }

        if(owner == null)
        {
            return true;
        }

        IEntityPlayerMP player = ModEnv.entityProtections.mod.playerOrFake(owner, world, x, y, z);
        return ModEnv.entityProtections.onEntityDamage(
                (IEntity) mcEntity,
                new EntityDamageSource(source.damageType, (Entity) player), amount, true
        );
    }

    @Referenced(at = ServerTickEventsFMLTransformer.class)
    public static boolean onSwapperSwap(EntityPlayer mcPlayer, ItemStack target, World mcWorld, int x, int y, int z)
    {
        IEntityPlayerMP player = (IEntityPlayerMP) mcPlayer;
        IItemStack stack = (IItemStack) (Object) target;
        IItem item = stack.getIItem();
        if(!(item instanceof ItemBlockBase))
            return true;

        Optional<Message> denial = ((ItemBlockBase) item).getIBlock().reactPrePlace(player, stack, new BlockPos(player.getServer().world(mcWorld), x, y, z))
                .can(player.getServer().mineCity, player);
        return denial.isPresent();
    }

    @Referenced(at = EntityPrimalOrbTransformer.class)
    public static boolean onEntityChangeBiome(Entity mcEntity, World mcWorld, int x, int z)
    {
        return !mcWorld.isRemote &&
                ModHooks.onEntityChangeBiome((IEntity) mcEntity, (IWorldServer) mcWorld, x, z).isPresent();

    }

    @Referenced(at = EntityFrostShardTransformer.class)
    public static boolean onEntityApplyNegativeEffect(EntityLivingBase affected, Entity applier)
    {
        return ModEnv.entityProtections.onEntityDamage((IEntity) affected, new EntityDamageSource("generic", applier), 1, true);
    }
}
