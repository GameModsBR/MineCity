package br.com.gamemods.minecity.forge.base.protection.opencomputers;

import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.shape.Point;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.ChunkPos;
import br.com.gamemods.minecity.forge.base.ForgeUtil;
import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.accessors.block.ITileEntity;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntity;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityLiving;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.entity.item.IEntityItem;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.accessors.world.IWorldServer;
import br.com.gamemods.minecity.forge.base.core.ModEnv;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.opencomputers.*;
import br.com.gamemods.minecity.forge.base.protection.ModHooks;
import br.com.gamemods.minecity.structure.ClaimedChunk;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fluids.IFluidHandler;
import scala.Option;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;

public class OCHooks
{
    private static Object colorModule;
    private static Method isDye;
    private static Constructor blockPos;
    private static Object wrenchModule;
    private static Method holdsApplicableWrench;
    private static Method isWrench;

    private static Object pos(int x, int y, int z)
    {
        try
        {
            if(blockPos == null)
            {
                Class<?> c;
                try
                {
                    c = Class.forName("net.minecraft.util.math.BlockPos");
                }
                catch(ClassNotFoundException e)
                {
                    c = Class.forName("li.cil.oc.util.BlockPosition");
                }
                blockPos = c.getConstructor(Integer.TYPE, Integer.TYPE, Integer.TYPE);
            }

            return blockPos.newInstance(x, y, z);
        }
        catch(ReflectiveOperationException e)
        {
            throw new UnsupportedOperationException(e);
        }
    }

    private static Object getWrenchModule() throws ReflectiveOperationException
    {
        if(wrenchModule == null)
            wrenchModule = Class.forName("li.cil.oc.integration.util.Wrench$").getDeclaredField("MODULE$").get(null);
        return wrenchModule;
    }

    public static boolean isWrench(IItemStack stack)
    {
        try
        {
            if(isWrench == null)
                isWrench = Class.forName("li.cil.oc.integration.util.Wrench$").getDeclaredMethod("isWrench", ItemStack.class);

            return (boolean) isWrench.invoke(getWrenchModule(), stack);
        }
        catch(ReflectiveOperationException e)
        {
            throw new UnsupportedOperationException(e);
        }
    }

    public static boolean holdsApplicableWrench(IEntityPlayerMP player, int x, int y, int z)
    {
        try
        {
            Object pos = pos(x, y, z);
            if(holdsApplicableWrench == null)
                holdsApplicableWrench = Class.forName("li.cil.oc.integration.util.Wrench$").getDeclaredMethod("holdsApplicableWrench", EntityPlayer.class, pos.getClass());

            return (boolean) holdsApplicableWrench.invoke(getWrenchModule(), player, pos);
        }
        catch(ReflectiveOperationException e)
        {
            throw new UnsupportedOperationException(e);
        }
    }

    private static Object getColorModule()
    {
        try
        {
            if(colorModule == null)
                colorModule = Class.forName("li.cil.oc.util.Color$").getDeclaredField("MODULE$").get(null);

            return colorModule;
        }
        catch(ReflectiveOperationException e)
        {
            throw new UnsupportedOperationException(e);
        }
    }

    public static boolean isDye(ItemStack stack)
    {
        try
        {
            Object module = getColorModule();
            if(isDye == null)
                isDye = Class.forName("li.cil.oc.util.Color$").getDeclaredMethod("isDye", ItemStack.class);
            return (boolean) isDye.invoke(module, stack);
        }
        catch(ReflectiveOperationException e)
        {
            throw new UnsupportedOperationException(e);
        }
    }

    @Referenced(at = PacketHandlerDTransformer.class)
    public static boolean onPlayerInteract(ITextBuffer buffer, IEntityPlayerMP player)
    {
        MineCityForge mod = player.getServer();

        IEnvironmentHost host = buffer.host();
        if(host instanceof IScreen)
        {
            BlockPos pos = host.envBlockPos(mod);
            return mod.mineCity.provideChunk(pos.getChunk()).getFlagHolder(pos).can(player,
                    PermissionFlag.CLICK
            ).isPresent();
        }

        return false;
    }

    @Referenced(at = InventoryWorldControlDClassTransformer.class)
    public static boolean onRobotDropItem(IInventory inventory, int slot, int amount, IAgentComponent agent, int face)
    {
        MineCityForge mod = ModEnv.entityProtections.mod;
        IEntityPlayerMP player = (IEntityPlayerMP) agent.fakePlayer();
        BlockPos pos = player.getBlockPos(mod).add(ForgeUtil.toDirection(face));
        return mod.mineCity.provideChunk(pos.getChunk()).getFlagHolder(pos).can(player, PermissionFlag.PICKUP).isPresent();
    }

    @Referenced(at = TankWorldControlDClassTransformer.class)
    public static boolean onRobotAccessTank(IAgentComponent agent, Option<IWorldServer> optionalWorld, int x, int y, int z)
    {
        if(optionalWorld.isEmpty())
            return true;

        IWorldServer world = optionalWorld.get();
        ITileEntity tile = world.getTileEntity(x, y, z);
        PermissionFlag flag = PermissionFlag.MODIFY;
        if(tile instanceof IFluidHandler || (!ModEnv.seven && tile instanceof net.minecraftforge.fluids.capability.IFluidHandler))
            flag = PermissionFlag.OPEN;

        MineCityForge mod = ModEnv.entityProtections.mod;
        return mod.mineCity.provideChunk(new ChunkPos(mod.world(world), x >> 4, z >> 4)).getFlagHolder(x, y, z)
                .can((IEntityPlayerMP) agent.fakePlayer(), flag).isPresent();
    }

    @Referenced(at = UpgradeLeashTransformer.class)
    public static List<IEntityLiving> onLeash(List<IEntityLiving> entities, IUpgradeLeash module)
    {
        MineCityForge mod = ModEnv.entityProtections.mod;
        IEntity host = (IEntity) module.host();
        IEntity permissible;
        if(host instanceof IAgentComponent)
        {
            IAgent agent = (IAgent) host;
            permissible = (IEntityPlayerMP) agent.player();
        }
        else
            permissible = host;

        BlockPos last = host.getBlockPos(mod);
        ClaimedChunk lastChunk = null;
        Iterator<IEntityLiving> iter = entities.iterator();
        while(iter.hasNext())
        {
            IEntityLiving entity = iter.next();
            if(entity.isLeashed())
                continue;

            if(host instanceof IEntityPlayerMP && !entity.canBeLeashedTo((IEntityPlayerMP) host))
                continue;

            BlockPos to = entity.getBlockPos(last);
            lastChunk = mod.mineCity.provideChunk(to.getChunk(), lastChunk);
            if(lastChunk.getFlagHolder(to).can(permissible, PermissionFlag.MODIFY).isPresent())
                iter.remove();
        }

        return entities;
    }

    @Referenced(at = UpgradeTractorBeamTransformer.class)
    @Referenced(at = MagnetProviderTransformer.class)
    public static List<IEntityItem> onSuck(List<IEntityItem> items, Hosted upgrade)
    {
        if(items.isEmpty())
            return items;

        Object owner = upgrade.host();
        IEntityPlayerMP player;
        if(owner instanceof IEntityPlayerMP)
            player = (IEntityPlayerMP) owner;
        else if(owner instanceof IAgent)
            player = (IEntityPlayerMP) ((IAgent) owner).player();
        else
        {
            items.clear();
            return items;
        }

        Iterator<IEntityItem> iter = items.iterator();
        while(iter.hasNext())
            if(ModEnv.entityProtections.onPlayerPickupItem(player, iter.next(), true))
                iter.remove();

        return items;
    }

    @Referenced(at = UpgradeTractorBeamTransformer.class)
    public static Object getPermissible(Object who)
    {
        if(who instanceof Hosted)
            who = ((Hosted) who).host();

        if(who instanceof IAgent)
            who = ((IAgent) who).ownerId();

        return who;
    }

    @Referenced(at = InventoryWorldControlMk2DClassTransformer.class)
    public static Option onInventoryControllerAccess(Option inv, IInventoryWorldControlMk2 controller, Option<World> worldOption, int x, int y, int z, int face)
    {
        if(worldOption.isEmpty())
            return null;

        World world = worldOption.get();
        BlockPos pos = new BlockPos(ModEnv.blockProtections.mod.world(world), x, y, z);

        EntityPlayer fakePlayer = controller.fakePlayer();
        if(ModEnv.blockProtections.onPlayerRightClickBlock(
                fakePlayer, false, null, ((IWorldServer) world).getIState(pos), pos, ForgeUtil.toDirection(face), false
        ) != 0)
        {
            return null;
        }

        return inv;
    }

    @Referenced(at = AdapterTransformer.class)
    public static boolean onAdapterAccess(IAdapter adapter, IWorldServer world, Point pos)
    {
        return ModHooks.onBlockAccessOther((World) world, pos.x, pos.y, pos.z,
                adapter.getPosX(), adapter.getPosY(), adapter.getPosZ(),
                PermissionFlag.MODIFY
        ).isPresent();
    }

    @Referenced(at = InventoryTransferDClassTransformer.class)
    public static Option onInventoryTransferAccess(Hosted hosted, BlockPos pos)
    {
        Object obj = hosted.host();
        BlockPos host;
        if(obj instanceof ITileEntity)
            host = ((ITileEntity) obj).getBlockPos(ModEnv.entityProtections.mod);
        else if(obj instanceof IEnvironmentHost)
            host = ((IEnvironmentHost) obj).envBlockPos(ModEnv.entityProtections.mod);
        else if(obj instanceof BlockPos)
            host = (BlockPos) obj;
        else
            return Option.empty();

        if(ModHooks.onBlockAccessOther(pos.world.getInstance(World.class),
                pos.x, pos.y, pos.z,
                host.x, host.y, host.z,
                PermissionFlag.OPEN).isPresent()
        )
        {
            return Option.empty();
        }

        return null;
    }
}
